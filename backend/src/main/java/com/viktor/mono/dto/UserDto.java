package com.viktor.mono.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.viktor.mono.dto.monobank.MonoAccountDto;
import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String publicName;
    @JsonIgnore
    private String monoToken;
    private boolean enabledOldTransactionsFetching;
    private String monoClientId;
    private String monoName;
    private List<MonoAccountDto> monoAccounts;
}
