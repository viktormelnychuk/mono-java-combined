package com.viktor.mono.controller;

import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.service.TransactionService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getOne(@PathVariable("id") Long id) {
        return ResponseEntity.ok(transactionService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<List<TransactionDto>> createOne(@RequestBody TransactionDto dto) {
        transactionService.createOne(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
