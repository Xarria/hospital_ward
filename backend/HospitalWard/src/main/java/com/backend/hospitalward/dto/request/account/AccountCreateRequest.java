package com.backend.hospitalward.dto.request.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {

    String password;

    String accessLevel;

    String name;

    String surname;

    String email;

}
