package com.backend.hospitalward.dto.response.account;

import com.backend.hospitalward.dto.BaseDTO;
import com.backend.hospitalward.model.AccessLevel;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.util.etag.SignableDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountDetailsDTO extends BaseDTO implements SignableDTO {

    String login;

    String type;

    String accessLevel;

    String name;

    String surname;

    String email;

    boolean active;

    boolean confirmed;

    Account createdBy;

    Timestamp creationDate = Timestamp.from(Instant.now());

    Account modifiedBy;

    Timestamp modificationDate;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
