package com.backend.hospitalward.dto.response.disease;

import com.backend.hospitalward.dto.BaseDTO;
import com.backend.hospitalward.util.etag.SignableDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DiseaseDetailsResponse extends BaseDTO implements SignableDTO {

    String name;

    String urgency;

    boolean cathererRequired;

    boolean surgeryRequired;

    String createdBy;

    Timestamp creationDate;

    String modifiedBy;

    Timestamp modificationDate;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
