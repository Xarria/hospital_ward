package com.backend.hospitalward.dto.response.patient;

import com.backend.hospitalward.dto.BaseDTO;
import com.backend.hospitalward.dto.response.disease.DiseaseGeneralResponse;
import com.backend.hospitalward.util.etag.SignableDTO;
import com.backend.hospitalward.util.serialization.LocalDateJsonDeserializer;
import com.backend.hospitalward.util.serialization.LocalDateJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PatientDetailsResponse extends BaseDTO implements SignableDTO {

    Long id;

    String pesel;

    String age;

    String sex;

    List<DiseaseGeneralResponse> diseases;

    String referralNr;

    @JsonDeserialize(using = LocalDateJsonDeserializer.class)
    @JsonSerialize(using = LocalDateJsonSerializer.class)
    LocalDate referralDate;

    String patientType;

    String mainDoctor;

    String covidStatus;

    String name;

    String surname;

    @JsonDeserialize(using = LocalDateJsonDeserializer.class)
    @JsonSerialize(using = LocalDateJsonSerializer.class)
    LocalDate admissionDate;

    String status;

    String phoneNumber;

    String emailAddress;

    boolean urgent;

    String createdBy;

    Timestamp creationDate;

    private String modifiedBy;

    private Timestamp modificationDate;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
