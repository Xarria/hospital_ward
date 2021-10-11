package com.backend.hospitalward.dto.response.account;

import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountGeneralResponse {

    String login;

    String type;

    String accessLevel;

    String name;

    String surname;

}
