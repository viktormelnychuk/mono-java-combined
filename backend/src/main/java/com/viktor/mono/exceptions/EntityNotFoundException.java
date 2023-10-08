package com.viktor.mono.exceptions;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityName) {
        super(String.format("%s not found", entityName));
    }
}
