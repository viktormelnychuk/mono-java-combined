package com.viktor.mono.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;
    private String monoId;
    private LocalDateTime time;
    private String description;
    private Long mcc;
    private String mccDescription;
    private Boolean hold;
    private Integer amount;
    private Integer operationAmount;
    private Integer currencyCode;
    private Integer comissionRate;
    private Integer cashbackAmount;
    private Integer balance;
    private String comment;
    private String receiptId;
    private String counterEdrpou;
    private String counterIban;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "mono_account_id")
    MonoAccount monoAccount;
}
