package com.backend.hospitalward.dto.request.medicalStaff;

import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class MedicalStaffUpdateRequest extends AccountUpdateRequest {

    String licenseNr;

    String academicDegree;

    List<String> specializations;

}
