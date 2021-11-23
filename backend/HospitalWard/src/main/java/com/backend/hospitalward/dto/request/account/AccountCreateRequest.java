package com.backend.hospitalward.dto.request.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {

    @NotBlank
    String accessLevel;

    @NotBlank
    String name;

    @NotBlank
    String surname;

    @Email
    @NotBlank
    @Size(max = 50)
    String email;

}
