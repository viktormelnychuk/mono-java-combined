package com.viktor.mono.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username")
        }
)
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;
    private String publicName;
    private String password;
    private String monoToken;
    private String monoClientId;
    private String monoName;

    @Column(columnDefinition = "boolean default false")
    private boolean enabledOldTransactionsFetching;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    List<Transaction> transactions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    List<MonoAccount> monoAccounts;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User() {

    }
}
