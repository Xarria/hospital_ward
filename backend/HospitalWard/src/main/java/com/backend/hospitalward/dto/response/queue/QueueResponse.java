package com.backend.hospitalward.dto.response.queue;

import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import com.backend.hospitalward.util.serialization.LocalDateJsonDeserializer;
import com.backend.hospitalward.util.serialization.LocalDateJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode()
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class QueueResponse {

    @JsonDeserialize(using = LocalDateJsonDeserializer.class)
    @JsonSerialize(using = LocalDateJsonSerializer.class)
    LocalDate date;

    List<PatientGeneralResponse> patientsWaiting;

    List<PatientGeneralResponse> patientsConfirmed;

    boolean locked;
}
