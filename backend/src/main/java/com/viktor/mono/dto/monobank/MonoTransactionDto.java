package com.viktor.mono.dto.monobank;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonoTransactionDto {
    private String id;
    private Long time;
    private String description;
    private Long mcc;
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
    private String counterName;
}
