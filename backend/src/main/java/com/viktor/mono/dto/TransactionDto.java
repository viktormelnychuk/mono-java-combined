package com.viktor.mono.dto;

import com.viktor.mono.dto.monobank.MonoAccountDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private String monoId;
    private LocalDateTime time;
    private String description;
    private long mcc;
    private String mccDescription;
    private boolean hold;
    private int amount;
    private int operationAmount;
    private int currencyCode;
    private int comissionRate;
    private int cashbackAmount;
    private int balance;
    private String comment;
    private String receiptId;
    private String counterEdrpou;
    private String counterIban;
    private LocalDateTime createdAt;
    private MonoAccountDto monoAccount;
}
