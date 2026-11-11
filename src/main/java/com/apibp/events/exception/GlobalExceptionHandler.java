package com.apibp.events.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ApiError(String code, String message) {}

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiError> handle(DomainException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiError(ex.getCode().name(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(ErrorCode.INTERNAL_ERROR.name(), "Unexpected error"));
    }
}
