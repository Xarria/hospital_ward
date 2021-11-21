package com.backend.hospitalward.dto.request.account;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class ResetPasswordRequest {

    @NotBlank
    @Email
    String email;

    @NotBlank
    @Pattern(regexp = "[A-Z][a-z]+")
    String nameDirector;

    @NotBlank
    @Pattern(regexp = "[A-Z][a-z]+")
    String surnameDirector;
}
