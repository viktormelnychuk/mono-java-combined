package com.viktor.mono.exceptions;

public class MonoInteractionException extends Exception {
    public MonoInteractionException(String responseBody) {
        super(String.format("Mono interaction failed with error %s", responseBody));
    }
}
