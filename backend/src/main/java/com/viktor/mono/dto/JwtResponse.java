package com.viktor.mono.dto;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private List<String> roles;

    public JwtResponse(String jwt, Long id, String username, List<String> roles) {
        this.token = jwt;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }
}
