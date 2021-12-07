package com.backend.hospitalward.dto.response.queue;

import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Date;
import java.util.List;

@EqualsAndHashCode()
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class QueueResponse {

    private Date date;

    List<PatientGeneralResponse> patientsWaiting;

    List<PatientGeneralResponse> patientsConfirmed;

    private boolean locked;
}
