package com.backend.hospitalward.exception;

import org.springframework.http.HttpStatus;

public class CommonException extends GeneralException {


    private static final String CONSTRAINT_VIOLATION = "error.constraint_violation";
    private static final String OPTIMISTIC_LOCK = "error.optimistic_lock";
    private static final String NO_RESULT = "error.no_result";
    private static final String JDBC_CONNECTION = "error.jdbc_connection";
    private static final String PRECONDITION_FAILED = "error.precondition_failed";
    private static final String ACCESS_DENIED = "error.access_denied";
    private static final String UNKNOWN = "error.unknown";
    private static final String CREDENTIALS_INVALID = "error.invalid_credentials";

    protected CommonException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public static CommonException createOptimisticLockException(String message) {
        return new CommonException(HttpStatus.CONFLICT, message);
    }

    public static CommonException createNoResultException() {
        return new CommonException(HttpStatus.GONE, NO_RESULT);
    }

    public static CommonException createJDBCConnectionException() {
        return new CommonException(HttpStatus.SERVICE_UNAVAILABLE, JDBC_CONNECTION);
    }

    public static CommonException createPreconditionFailedException() {
        return new CommonException(HttpStatus.PRECONDITION_FAILED, PRECONDITION_FAILED);
    }

    public static CommonException createForbiddenException() {
        return new CommonException(HttpStatus.FORBIDDEN, ACCESS_DENIED);
    }

    public static CommonException createUnknownException() {
        return new CommonException(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN);
    }

    public static CommonException createConstraintViolationException() {
        return new CommonException(HttpStatus.BAD_REQUEST, CONSTRAINT_VIOLATION);
    }

    public static CommonException createUnauthorizedException() {
        return new CommonException(HttpStatus.UNAUTHORIZED, CREDENTIALS_INVALID);
    }
}
