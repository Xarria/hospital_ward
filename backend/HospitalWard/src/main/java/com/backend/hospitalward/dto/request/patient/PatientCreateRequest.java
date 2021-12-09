package com.backend.hospitalward.dto.request.patient;

import com.backend.hospitalward.util.serialization.LocalDateJsonDeserializer;
import com.backend.hospitalward.util.serialization.LocalDateJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

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

    @JsonSerialize(using = LocalDateJsonSerializer.class)
    @JsonDeserialize(using = LocalDateJsonDeserializer.class)
    LocalDate referralDate;

    String mainDoctor;

    String covidStatus;

    String name;

    String surname;

    @JsonSerialize(using = LocalDateJsonSerializer.class)
    @JsonDeserialize(using = LocalDateJsonDeserializer.class)
    LocalDate admissionDate;

    String phoneNumber;

    String emailAddress;

    boolean urgent;
}
