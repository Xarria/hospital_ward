package com.backend.hospitalward.exception;

public final class ErrorKey {

    public static final String CRITICAL = "error.general.critical";
    public static final String PERSISTENCE = "error.persistence.unknown";
    public static final String CONSTRAINT_VIOLATION = "error.persistence.constraint_violation";
    public static final String CONNECTION = "error.persistence.connection";
    public static final String OPTIMISTIC_LOCK = "error.persistence.optimistic_lock";
    public static final String NO_RESULT = "error.persistence.no_result";
    public static final String TIMED_OUT = "error.persistence.timed_out";
}
