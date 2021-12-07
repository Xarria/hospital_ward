package com.backend.hospitalward.dto.response.patient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@EqualsAndHashCode()
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PatientGeneralResponse {

    Long id;

    String age;

    String sex;

    String referralNr;

    Timestamp referralDate;

    String patientType;

    String mainDoctor;

    String name;

    String surname;

    Timestamp admissionDate;

    String status;

    boolean urgent;

    boolean cathererRequired;

    boolean surgeryRequired;

}
