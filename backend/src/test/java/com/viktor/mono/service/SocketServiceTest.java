package com.viktor.mono.service;

import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.dto.UserDto;
import com.viktor.mono.security.services.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SocketServiceTest {

    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    SocketService socketService;

    @Test
    void shouldNotifyCorrectUserWithListOfTransactions() {
        List<TransactionDto> transactions = Arrays.asList(mock(TransactionDto.class), mock(TransactionDto.class));
        UserDto userDto = new UserDto();
        userDto.setUsername("admin");
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> transactionsCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Map<String, Object>> headerCaptor = ArgumentCaptor.forClass(Map.class);


        socketService.notifyTransaction(userDto, transactions);

        verify(simpMessagingTemplate).convertAndSendToUser(usernameCaptor.capture(),
                destinationCaptor.capture(),
                transactionsCaptor.capture(),
                headerCaptor.capture());
        assertEquals(userDto.getUsername(), usernameCaptor.getValue());
        assertEquals("/transactions", destinationCaptor.getValue());
        List<TransactionDto> sentTransactions = (List<TransactionDto>) transactionsCaptor.getValue();
        Map<String, Object> sentHeaders = headerCaptor.getValue();

        assertEquals(transactions, sentTransactions);
        assertEquals(SimpMessageType.MESSAGE, sentHeaders.get("simpMessageType"));
        assertEquals(userDto.getUsername(), sentHeaders.get("simpSessionId"));
    }

}