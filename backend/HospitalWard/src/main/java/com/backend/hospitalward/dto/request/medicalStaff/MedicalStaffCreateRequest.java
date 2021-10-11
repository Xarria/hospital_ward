package com.backend.hospitalward.dto.request.medicalStaff;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MedicalStaffCreateRequest extends AccountCreateRequest {

    String licenseNr;

    String academicDegree;

    List<String> specializations;
}
