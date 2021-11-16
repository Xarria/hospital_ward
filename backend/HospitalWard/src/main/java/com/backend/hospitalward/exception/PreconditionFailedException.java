package com.backend.hospitalward.exception;

public class PreconditionFailedException extends ApplicationException {

    public PreconditionFailedException(String message) {
        super(message);
    }
}
