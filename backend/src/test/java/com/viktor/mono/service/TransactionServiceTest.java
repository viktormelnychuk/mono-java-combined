package com.viktor.mono.service;

import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.dto.UserDto;
import com.viktor.mono.entity.MonoAccount;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.entity.User;
import com.viktor.mono.exceptions.EntityNotFoundException;
import com.viktor.mono.mapper.*;
import com.viktor.mono.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository transactionRepository;
    @Mock
    UserService userService;
    @Mock
    UserMapper userMapper;
    @Mock
    SocketService socketService;

    @Mock
    TransactionMapper mapper;


    @InjectMocks
    TransactionService transactionService;


    @Test
    void shouldReturnAllDtos() {
        List<Transaction> entities = Arrays.asList(mock(Transaction.class), mock(Transaction.class));
        when(transactionRepository.findAll()).thenReturn(entities);
        when(mapper.fromEntity(any(Transaction.class))).thenReturn(mock(TransactionDto.class));

        List<TransactionDto> allDtos = transactionService.getAll();
        assertEquals(2, allDtos.size());
        assertNotNull(allDtos.get(0));
        assertNotNull(allDtos.get(1));
    }

    @Test
    void shouldReturnSingleDtoIfExists() {
        Transaction transaction = mock(Transaction.class);
        TransactionDto transactionDto = mock(TransactionDto.class);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(mapper.fromEntity(transaction)).thenReturn(transactionDto);

        TransactionDto returned = transactionService.getOne(1L);

        assertEquals(transactionDto, returned);
    }

    @Test
    void shouldThrowExceptionIfTransactionDoesNotExist() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transactionService.getOne(1L);
        });
        assertEquals("transaction not found", exception.getMessage());
    }

    @Test
    void createOneShouldSaveAndNotify() {
        Transaction entity = mock(Transaction.class);
        TransactionDto transactionDto = mock(TransactionDto.class);
        when(mapper.fromDto(transactionDto)).thenReturn(entity);
        when(mapper.fromEntity(entity)).thenReturn(transactionDto);
        UserDto currentUser = mock(UserDto.class);
        when(userService.getCurrent()).thenReturn(currentUser);

        transactionService.createOne(transactionDto);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(entity, transactionCaptor.getValue());

        ArgumentCaptor<UserDto> userDtoCaptor = ArgumentCaptor.forClass(UserDto.class);
        ArgumentCaptor<List<TransactionDto>> transactionDtosCaptor = ArgumentCaptor.forClass(List.class);

        verify(socketService).notifyTransaction(userDtoCaptor.capture(), transactionDtosCaptor.capture());

        assertEquals(currentUser, userDtoCaptor.getValue());
        assertEquals(Collections.singletonList(transactionDto), transactionDtosCaptor.getValue());

    }

    @Test
    void shouldSetCorrectValuesForTransactionWhenCreatedWithMonoAccount() {
        Transaction entity = mock(Transaction.class);
        TransactionDto transactionDto = mock(TransactionDto.class);
        when(mapper.fromDto(transactionDto)).thenReturn(entity);
        when(mapper.fromEntity(entity)).thenReturn(transactionDto);
        UserDto currentUser = mock(UserDto.class);

        MonoAccount monoAccount = mock(MonoAccount.class);
        User user = mock(User.class);
        when(monoAccount.getUser()).thenReturn(user);
        when(userMapper.fromEntity(user)).thenReturn(currentUser);

        transactionService.create(transactionDto, monoAccount);

        verify(entity).setMonoAccount(monoAccount);
        verify(entity).setUser(user);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(entity, transactionCaptor.getValue());

        ArgumentCaptor<UserDto> userDtoCaptor = ArgumentCaptor.forClass(UserDto.class);
        ArgumentCaptor<List<TransactionDto>> transactionDtosCaptor = ArgumentCaptor.forClass(List.class);

        verify(socketService).notifyTransaction(userDtoCaptor.capture(), transactionDtosCaptor.capture());

        assertEquals(currentUser, userDtoCaptor.getValue());
        assertEquals(Collections.singletonList(transactionDto), transactionDtosCaptor.getValue());
    }
}