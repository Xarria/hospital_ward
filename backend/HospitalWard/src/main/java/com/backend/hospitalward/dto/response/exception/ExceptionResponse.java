package com.backend.hospitalward.dto.response.exception;

import com.backend.hospitalward.util.serialization.TimestampJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value
public class ExceptionResponse {

    @JsonSerialize(using = TimestampJsonSerializer.class)
    Timestamp timestamp;
    String message;


    private ExceptionResponse(String messages) {
        this.message = messages;
        timestamp = Timestamp.from(Instant.now());

    }

    public static ExceptionResponse singleException(String message) {
        return new ExceptionResponse(message);
    }

    public static ExceptionResponse manyExceptions(String messages) {
        return new ExceptionResponse(messages);
    }
}
