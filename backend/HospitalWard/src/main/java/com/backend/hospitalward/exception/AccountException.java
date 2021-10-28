package com.backend.hospitalward.exception;

import org.springframework.http.HttpStatus;

public class AccountException extends GeneralException{

    public static final String ACCOUNT_NOT_FOUND = "error.account_not_found";

    protected AccountException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public static AccountException createNotFoundException(String key) {
        return new AccountException(HttpStatus.NOT_FOUND, key);
    }

}
