package com.backend.hospitalward.exception;

public final class ErrorKey {

    //region common
    public static final String CRITICAL = "error.critical";
    public static final String PERSISTENCE = "error.unknown";
    public static final String CONSTRAINT_VIOLATION = "error.constraint_violation";
    public static final String CONNECTION = "error.database_connection";
    public static final String OPTIMISTIC_LOCK = "error.optimistic_lock";
    public static final String NO_RESULT = "error.no_result";
    public static final String TIMED_OUT = "error.transaction_timed_out";
    public static final String ETAG_INVALID = "error.invalid_etag";
    public static final String JWT_INVALID = "error.jwt_token_invalid";
    public static final String ACCESS_DENIED = "error.access_denied";
    public static final String UNKNOWN = "error.unknown";
    public static final String CREDENTIALS_INVALID = "error.invalid_credentials";
    //endregion

    //region account
    public static final String ACCOUNT_NOT_FOUND = "error.account_not_found";
    public static final String PASSWORD_INCORRECT = "error.incorrect_password";
    public static final String PASSWORD_THE_SAME = "error.new_password_the_same_as_old";
    public static final String EMAIL_UNIQUE = "error.email_not_unique";
    public static final String ERROR_SAME_PASSWORD = "error.new_password_same_as_old";
    public static final String NOT_OWN_ACCOUNT = "error.account_not_owned_by_current_user";
    public static final String INVALID_LEVEL_OR_LOGIN = "error.invalid_access_level_or_login";
    public static final String EMAIL_INVALID = "error.email_invalid";
    //endregion

    //region accessLevel
    public static final String ACCESS_LEVEL_NOT_FOUND = "error.non_existing_access_level";
    public static final String OFFICE_STAFF_ACCESS_LEVEL_CHANGE = "error.office_staff_access_level_change_not_possible";
    public static final String MEDICAL_STAFF_TO_OFFICE_CHANGE = "error.medical_staff_access_level_change_to_office_not_possible";
    public static final String TREATMENT_DIRECTOR_REQUIRED = "error.at_least_one_treatment_director_required";
    public static final String HEAD_NURSE_REQUIRED = "error.at_least_one_head_nurse_required";
    public static final String ACCESS_LEVEL_INVALID_MEDIC = "error.accessLevel_office_for_type_medic";
    public static final String ACCESS_LEVEL_INVALID_OFFICE = "error.accessLevel_medic_for_type_office";
    //endregion

    //region medicalStaff
    public static final String LICENSE_NUMBER = "error.license_nr_invalid";
    public static final String SPECIALIZATION_NOT_FOUND = "error.specialization_not_found";
    //endregion

    //region url
    public static final String URL_NOT_FOUND = "error.url_not_found";
    public static final String URL_EXPIRED = "error.url_expired";
    public static final String URL_WRONG_ACTION = "error.invalid_action_type";
    public static final String URL_INVALID = "error.url_code_invalid";
    //endregion

}
