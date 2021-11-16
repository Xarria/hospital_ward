package com.backend.hospitalward.exception;

public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(message);
    }
}
