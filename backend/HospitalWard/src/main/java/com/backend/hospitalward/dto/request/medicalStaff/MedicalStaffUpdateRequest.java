package com.backend.hospitalward.dto.request.medicalStaff;

import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalStaffUpdateRequest extends AccountUpdateRequest {

    String licenseNr;

    String academicDegree;

    List<String> specializations;

}
