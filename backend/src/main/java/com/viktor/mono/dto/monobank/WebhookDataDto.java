package com.viktor.mono.dto.monobank;

import lombok.Data;

@Data
public class WebhookDataDto {
    private String account;
    private StatementItemDto statementItem;
}
