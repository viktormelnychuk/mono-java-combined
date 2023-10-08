package com.viktor.mono.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Mcc {

    @GeneratedValue
    @Id
    @JsonIgnore
    Long id;

    private String description;
    private Long code;
}
