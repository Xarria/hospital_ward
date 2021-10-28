package com.backend.hospitalward.dto.response.exception;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Value
public class ExceptionResponse {

    LocalDateTime dateTime;
    Set<BaseError> errors;


    private ExceptionResponse(Set<BaseError> errors) {
        this.errors = errors;
        dateTime = LocalDateTime.now();
    }

    public static ExceptionResponse singleException(String key, String message) {
        return new ExceptionResponse(Collections.singleton(new BaseError(key, message)));
    }

    public static ExceptionResponse manyExceptions(Set<BaseError> errors) {
        return new ExceptionResponse(errors);
    }
}
