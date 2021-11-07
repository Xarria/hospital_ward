package com.backend.hospitalward.dto.response.account;

import com.backend.hospitalward.dto.BaseDTO;
import com.backend.hospitalward.dto.response.medicalStaff.MedicalStaffGeneralDTO;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MedicalStaffGeneralDTO.class, name = "medic"),
})
public class AccountGeneralDTO extends BaseDTO {

    String login;

    String type;

    String accessLevel;

    String name;

    String surname;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
