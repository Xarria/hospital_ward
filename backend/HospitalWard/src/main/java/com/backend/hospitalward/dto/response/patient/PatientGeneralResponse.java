package com.backend.hospitalward.dto.response.patient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.LocalDate;

@EqualsAndHashCode()
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PatientGeneralResponse {

    Long id;

    String age;

    String sex;

    String referralNr;

    LocalDate referralDate;

    String patientType;

    String mainDoctor;

    String name;

    String surname;

    int positionInQueue;

    LocalDate admissionDate;

    String status;

    boolean urgent;

    boolean cathererRequired;

    boolean surgeryRequired;

}
