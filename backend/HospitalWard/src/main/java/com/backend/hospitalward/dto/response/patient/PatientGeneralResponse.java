package com.backend.hospitalward.dto.response.patient;

import com.backend.hospitalward.util.serialization.LocalDateJsonDeserializer;
import com.backend.hospitalward.util.serialization.LocalDateJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    String patientType;

    String name;

    String surname;

    int positionInQueue;

    @JsonDeserialize(using = LocalDateJsonDeserializer.class)
    @JsonSerialize(using = LocalDateJsonSerializer.class)
    LocalDate admissionDate;

    String status;

    boolean urgent;

    boolean cathererRequired;

    boolean surgeryRequired;

}
