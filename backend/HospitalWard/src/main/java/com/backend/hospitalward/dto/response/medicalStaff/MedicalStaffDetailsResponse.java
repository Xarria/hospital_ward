package com.backend.hospitalward.dto.response.medicalStaff;

import com.backend.hospitalward.dto.response.account.AccountDetailsResponse;
import com.backend.hospitalward.model.Specialization;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalStaffDetailsResponse extends AccountDetailsResponse {

    String licenseNr;

    String academicDegree;

    List<Specialization> specializations;
}
