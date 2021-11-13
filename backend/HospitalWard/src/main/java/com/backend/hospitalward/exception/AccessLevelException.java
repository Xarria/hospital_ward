package com.backend.hospitalward.exception;

import org.springframework.http.HttpStatus;

public class AccessLevelException extends GeneralException{

    public static final String ACCESS_LEVEL_NOT_FOUND = "error.non_existing_access_level";
    public static final String OFFICE_STAFF_ACCESS_LEVEL_CHANGE = "error.office_staff_access_level_change_not_possible";
    public static final String MEDICAL_STAFF_TO_OFFICE_CHANGE = "error.medical_staff_access_level_change_to_office_not_possible";
    public static final String TREATMENT_DIRECTOR_REQUIRED = "error.at_least_one_treatment_director_required";
    public static final String HEAD_NURSE_REQUIRED = "error.at_least_one_head_nurse_required";

    protected AccessLevelException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public static AccessLevelException createNotFoundException(String key) {
        return new AccessLevelException(HttpStatus.NOT_FOUND, key);
    }

    public static AccessLevelException createConflictException(String key) {
        return new AccessLevelException(HttpStatus.CONFLICT, key);
    }
}
