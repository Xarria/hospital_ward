package com.backend.hospitalward.dto.request.account;

import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountUpdateGeneralRequest {

    String name;

    String surname;

    String email;
}
