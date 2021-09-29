package com.backend.hospitalward.dto.response;

import com.backend.hospitalward.model.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountGeneralResponse {

    String login;

    String type;

    AccessLevel accessLevel;

    String name;

    String surname;

}
