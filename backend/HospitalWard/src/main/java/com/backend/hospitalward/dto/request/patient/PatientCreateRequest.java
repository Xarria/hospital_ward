package com.backend.hospitalward.dto.request.patient;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode()
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PatientCreateRequest {

    String pesel;

    String age;

    String sex;

    List<String> diseases;

    String referralNr;

    LocalDate referralDate;

    String mainDoctor;

    String covidStatus;

    String name;

    String surname;

    LocalDate admissionDate;

    String phoneNumber;

    String emailAddress;

    boolean urgent;
}
