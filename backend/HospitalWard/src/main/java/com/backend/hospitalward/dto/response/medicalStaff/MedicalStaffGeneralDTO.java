package com.backend.hospitalward.dto.response.medicalStaff;

import com.backend.hospitalward.dto.response.account.AccountGeneralDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalStaffGeneralDTO extends AccountGeneralDTO {

    String licenseNr;
}
