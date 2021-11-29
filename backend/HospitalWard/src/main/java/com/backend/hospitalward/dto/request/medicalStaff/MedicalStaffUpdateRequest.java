package com.backend.hospitalward.dto.request.medicalStaff;

import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class MedicalStaffUpdateRequest extends AccountUpdateRequest {

    @Size(max = 8, min = 7)
    @Pattern(regexp = "[0-9]{7}[P]?")
    String licenseNr;

    //TODO walidator
    String academicDegree;

    List<String> specializations;

}
