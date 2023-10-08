package com.viktor.mono.service;

import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.dto.UserDto;
import com.viktor.mono.entity.MonoAccount;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.entity.User;
import com.viktor.mono.exceptions.EntityNotFoundException;
import com.viktor.mono.mapper.TransactionMapper;
import com.viktor.mono.mapper.UserMapper;
import com.viktor.mono.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final UserService userService;
    private final SocketService socketService;
    private final UserMapper userMapper;

    public TransactionService(TransactionRepository repository, TransactionMapper mapper, UserService userService, SocketService socketService, UserMapper userMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
        this.socketService = socketService;
        this.userMapper = userMapper;
    }

    public List<TransactionDto> getAll() {
        List<Transaction> transactions = repository.findAll();
        return transactions.stream().map(mapper::fromEntity).collect(Collectors.toList());
    }

    public TransactionDto getOne(Long id) {
        Transaction transaction = repository.findById(id).orElseThrow(() -> {
            return new EntityNotFoundException("transaction");
        });
        return mapper.fromEntity(transaction);
    }

    public void createOne(TransactionDto dto) {
        Transaction entity = mapper.fromDto(dto);
        UserDto currentUser = userService.getCurrent();
        saveAndNotify(entity, currentUser);
    }

    public void create(TransactionDto transactionDto, MonoAccount monoAccount) {
        Transaction transaction = mapper.fromDto(transactionDto);
        transaction.setMonoAccount(monoAccount);
        User user = monoAccount.getUser();
        transaction.setUser(monoAccount.getUser());

        saveAndNotify(transaction, userMapper.fromEntity(user));
    }

    private void saveAndNotify(Transaction entity, UserDto currentUser) {
        repository.save(entity);
        socketService.notifyTransaction(currentUser, Collections.singletonList(mapper.fromEntity(entity)));
    }
}
