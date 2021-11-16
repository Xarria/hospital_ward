package com.backend.hospitalward.exception;

public class ConstraintViolationException extends ApplicationException {

    public ConstraintViolationException(String message) {
        super(message);
    }
}
