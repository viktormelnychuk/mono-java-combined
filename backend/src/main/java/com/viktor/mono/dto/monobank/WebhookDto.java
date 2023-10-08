package com.viktor.mono.dto.monobank;

import lombok.Data;

@Data
public class WebhookDto {
    private String type;
    private WebhookDataDto data;
}
