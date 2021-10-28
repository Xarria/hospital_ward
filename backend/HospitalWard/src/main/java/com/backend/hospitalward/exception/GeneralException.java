package com.backend.hospitalward.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class GeneralException extends ResponseStatusException {

    protected GeneralException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
