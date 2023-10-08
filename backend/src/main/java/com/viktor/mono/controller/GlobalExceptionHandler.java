package com.viktor.mono.controller;

import com.viktor.mono.dto.ApiError;
import com.viktor.mono.exceptions.EntityNotFoundException;
import com.viktor.mono.exceptions.MonoInteractionException;
import com.viktor.mono.exceptions.UnprocessableEntityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UnprocessableEntityException.class)
    protected ResponseEntity<?> handleUprocessableEntityException(UnprocessableEntityException ex) {
        ApiError error = new ApiError();
        error.setStatus(HttpStatus.BAD_REQUEST);
        error.setMessage("can't process entity");
        error.setDebugMessage(ex.getMessage());
        return buildResponseEntity(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex) {
        ApiError error = new ApiError();
        error.setStatus(HttpStatus.NOT_FOUND);
        error.setMessage("not found");
        error.setDebugMessage(ex.getMessage());
        return buildResponseEntity(error);
    }

    @ExceptionHandler(MonoInteractionException.class)
    protected ResponseEntity<?> handleMonoInteractionException(MonoInteractionException ex) {
        ApiError error = new ApiError();
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        error.setMessage("error sending request to monobank api");
        error.setDebugMessage(ex.getMessage());
        return buildResponseEntity(error);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError error) {
        return new ResponseEntity<>(error, error.getStatus());
    }
}
