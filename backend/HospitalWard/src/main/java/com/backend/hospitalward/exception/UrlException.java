package com.backend.hospitalward.exception;

import org.springframework.http.HttpStatus;

public class UrlException extends GeneralException{
    public static final String URL_NOT_FOUND = "error.url_not_found";
    public static final String URL_EXPIRED = "error.url_expired";
    public static final String URL_WRONG_ACTION = "error.invalid_action_type";

    protected UrlException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public static UrlException createNotFoundException(String key) {
        return new UrlException(HttpStatus.NOT_FOUND, key);
    }

    public static UrlException createGoneException(String key) {
        return new UrlException(HttpStatus.GONE, key);
    }

    public static UrlException createBadRequestException(String key) {
        return new UrlException(HttpStatus.BAD_REQUEST, key);
    }
}
