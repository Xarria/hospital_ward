package com.backend.hospitalward.dto.response.exception;

import lombok.Value;

@Value
public class BaseError {

    String key;
    String message;
}
