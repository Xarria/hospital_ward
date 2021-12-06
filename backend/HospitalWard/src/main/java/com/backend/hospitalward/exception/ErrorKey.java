package com.backend.hospitalward.exception;

public final class ErrorKey {

    //region common
    public static final String ETAG_INVALID = "error.invalid_etag";
    public static final String ACCESS_DENIED = "error.access_denied";
    public static final String UNKNOWN = "error.unknown";
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
    public static final String ACCOUNT_NOT_CONFIRMED = "error.account_unconfirmed";
    public static final String ACCOUNT_CONFIRMED = "error.account_confirmed";
    //endregion

    //region disease
    public static final String DISEASE_NOT_FOUND = "error.disease_not_found";
    public static final String DISEASE_ASSIGNED_TO_PATIENT = "error.disease_assigned_to_patient";
    public static final String DISEASE_URGENCY_NOT_FOUND = "error.disease_urgency_not_found";

    //endregion

    //region patient
    public static final String PATIENT_NOT_FOUND = "error.patient_not_found";
    public static final String MAIN_DOCTOR_NOT_MEDIC = "error.main_doctor_is_not_medical_staff";
    public static final String COVID_STATUS_NOT_FOUND = "error.covid_status_not_found";
    public static final String PATIENT_TYPE_NOT_FOUND = "error.patient_type_not_found";
    public static final String PATIENT_STATUS_NOT_FOUND = "error.patient_status_not_found";
    public static final String PATIENT_CONFIRMED = "error.patient_already_confirmed";
    public static final String NO_PERMISSION_TO_CREATE_URGENT_PATIENT = "error.no_permission_to_create_urgent_patient";

    //endregion

    //region queue
    public static final String QUEUE_NOT_FOUND = "error.queue_not_found";
    public static final String QUEUE_LOCKED_OR_FULL = "error.queue_for_date_is_locked_or_full";
    public static final String QUEUE_LOCKED = "error.queue_locked";
    public static final String PATIENT_WRONG_QUEUE = "error.patient_does_not_belong_to_queue_for_provided_date";
    public static final String ADMISSION_DATE_WEEKEND = "error.admission_date_weekend";
    public static final String INVALID_ADMISSION_DATE = "error.admission_date_weekend_or_friday";
    public static final String PATIENT_ALREADY_ADMITTED = "error.patient_already_admitted";
    //endregion

}
