package com.backend.hospitalward.dto.response.medicalStaff;

import com.backend.hospitalward.dto.response.account.AccountGeneralResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalStaffGeneralResponse extends AccountGeneralResponse {

    String licenseNr;
}
