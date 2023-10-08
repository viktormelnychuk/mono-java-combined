package com.viktor.mono.dto.monobank;

import lombok.Data;

import java.util.List;

@Data
public class MonoAccountDto {
    private String id;
    private String monoAccountId;
    private String sendId;
    private int currencyCode;
    private String cashbackType;
    private Long balance;
    private Long creditLimit;
    private String type;
    private String iban;
}
