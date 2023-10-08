package com.viktor.mono.dto.monobank;

import lombok.Data;

import java.util.List;

@Data
public class ClientInfoDto {
    private String clientId;
    private String name;
    private String webHookUrl;
    private String permissions;
    List<MonoAccountDto> accounts;
}
