package com.backend.hospitalward.dto.response.exception;

import lombok.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value
public class ExceptionResponse {

    String dateTime;
    String message;


    private ExceptionResponse(String messages) {
        this.message = messages;
        dateTime = (LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
    }

    public static ExceptionResponse singleException(String message) {
        return new ExceptionResponse(message);
    }

    public static ExceptionResponse manyExceptions(String messages) {
        return new ExceptionResponse(messages);
    }
}
