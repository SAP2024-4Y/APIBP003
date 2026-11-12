package com.apibp.events.exception;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus status;

    public DomainException(ErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public ErrorCode getCode()     { return code; }
    public HttpStatus getStatus()  { return status; }
}
