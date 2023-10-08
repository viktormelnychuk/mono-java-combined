package com.viktor.mono.exceptions;

public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String entityName, String field, String reason) {
        super(String.format("can't process %s because [%s] %s", entityName, field, reason));
    }
}
