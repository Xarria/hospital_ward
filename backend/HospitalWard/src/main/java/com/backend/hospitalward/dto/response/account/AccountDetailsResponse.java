package com.backend.hospitalward.dto.response.account;

import com.backend.hospitalward.dto.BaseDTO;
import com.backend.hospitalward.util.etag.SignableDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountDetailsResponse extends BaseDTO implements SignableDTO {

    String login;

    String type;

    String accessLevel;

    String name;

    String surname;

    String email;

    boolean active;

    boolean confirmed;

    String createdBy;

    Timestamp creationDate;

    String modifiedBy;

    Timestamp modificationDate;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
