package com.viktor.mono.dto.monobank;

import lombok.Data;

@Data
public class StatementItemDto {
    private String id;
    private Long time;
    private String description;
    private Long mcc;
    private Long originalMcc;
    private Long amount;
    private Long operationAmount;
    private int currencyCode;
    private int commissionRate;
    private Long cashbackAmount;
    private Long balance;
    private boolean hold;
}
