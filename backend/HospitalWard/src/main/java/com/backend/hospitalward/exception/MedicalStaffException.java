package com.backend.hospitalward.exception;

import org.springframework.http.HttpStatus;

public class MedicalStaffException extends GeneralException {

    public static final String LICENSE_NUMBER = "error.license_nr_invalid";

    protected MedicalStaffException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public static MedicalStaffException createBadRequestException(String key) {
        return new MedicalStaffException(HttpStatus.BAD_REQUEST, key);
    }
}
