package com.backend.hospitalward.dto.response.account;

import com.backend.hospitalward.model.AccessLevel;
import com.backend.hospitalward.model.Account;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.Instant;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountDetailsResponse {

    String login;

    String type;

    AccessLevel accessLevel;

    String name;

    String surname;

    String email;

    boolean active;

    boolean confirmed;

    Account createdBy;

    Timestamp creationDate = Timestamp.from(Instant.now());

    Account modifiedBy;

    Timestamp modificationDate;
}
