package com.backend.hospitalward.dto.request.medicalStaff;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MedicalStaffCreateRequest extends AccountCreateRequest {

    @NotBlank
    @Size(max = 8, min = 7)
    @Pattern(regexp = "[0-9]{7}[P]?")
    String licenseNr;

    //TODO walidator po uzupełnieniu tabel
    String academicDegree;

    List<String> specializations;
}
