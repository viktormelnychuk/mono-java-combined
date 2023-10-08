package com.viktor.mono.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class MonoAccount {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "mono_account")
    private String monoAccountId;
    private String sendId;
    private Integer currencyCode;
    private String cashbackType;
    private Long balance;
    private Long creditLimit;
    private String type;
    private String iban;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
