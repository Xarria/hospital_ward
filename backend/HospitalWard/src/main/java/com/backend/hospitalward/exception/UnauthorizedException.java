package com.backend.hospitalward.exception;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
