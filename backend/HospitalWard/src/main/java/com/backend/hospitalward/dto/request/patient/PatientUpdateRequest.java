package com.backend.hospitalward.dto.request.patient;

import com.backend.hospitalward.dto.BaseDTO;
import com.backend.hospitalward.util.etag.SignableDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PatientUpdateRequest extends BaseDTO implements SignableDTO {

    Long id;

    String pesel;

    String age;

    String sex;

    List<String> diseases;

    String mainDoctor;

    String covidStatus;

    String name;

    String surname;

    String phoneNumber;

    String emailAddress;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
