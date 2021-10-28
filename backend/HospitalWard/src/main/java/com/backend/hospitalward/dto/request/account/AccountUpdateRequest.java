package com.backend.hospitalward.dto.request.account;

import com.backend.hospitalward.model.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;


@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountUpdateRequest {

    String login;

    AccessLevel accessLevel;

    String name;

    String surname;

    String email;
}
