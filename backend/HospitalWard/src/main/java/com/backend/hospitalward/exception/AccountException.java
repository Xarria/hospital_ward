package com.backend.hospitalward.exception;

import org.springframework.http.HttpStatus;

public class AccountException extends GeneralException{

    public static final String ACCOUNT_NOT_FOUND = "error.account_not_found";
    public static final String PASSWORD_INCORRECT = "error.incorrect_password";
    public static final String PASSWORD_THE_SAME = "error.new_password_the_same_as_old";

    protected AccountException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public static AccountException createNotFoundException(String key) {
        return new AccountException(HttpStatus.NOT_FOUND, key);
    }

    public static AccountException createBadRequestException(String key) {
        return new AccountException(HttpStatus.BAD_REQUEST, key);
    }

    public static AccountException createConflictException(String key) {
        return new AccountException(HttpStatus.CONFLICT, key);
    }
}
