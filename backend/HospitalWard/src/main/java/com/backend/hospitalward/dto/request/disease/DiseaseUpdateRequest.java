package com.backend.hospitalward.dto.request.disease;

import com.backend.hospitalward.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DiseaseUpdateRequest extends BaseDTO {

    String latinName;

    boolean cathererRequired;

    boolean surgeryRequired;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
