package com.backend.hospitalward.exception;

import org.springframework.http.HttpStatus;

public class PatientException extends GeneralException {

    protected PatientException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
