package com.backend.hospitalward.dto.response.exception;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Value
public class ExceptionResponse {

    LocalDateTime dateTime;
    Set<String> messages;


    private ExceptionResponse(Set<String> messages) {
        this.messages = messages;
        dateTime = LocalDateTime.now();
    }

    public static ExceptionResponse singleException(String message) {
        return new ExceptionResponse(Collections.singleton(message));
    }

    public static ExceptionResponse manyExceptions(Set<String> messages) {
        return new ExceptionResponse(messages);
    }
}
