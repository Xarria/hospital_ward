package com.backend.hospitalward.dto.response.disease;

import com.backend.hospitalward.dto.BaseDTO;
import com.backend.hospitalward.util.etag.SignableDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DiseaseResponse extends BaseDTO implements SignableDTO {

    String name;

    boolean urgent;

    boolean cathererRequired;

    boolean surgeryRequired;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
