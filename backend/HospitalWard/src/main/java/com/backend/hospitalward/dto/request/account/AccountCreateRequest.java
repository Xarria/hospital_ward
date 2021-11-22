package com.backend.hospitalward.dto.request.account;

import com.backend.hospitalward.util.validation.AccessLevelValid;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {

    @NotBlank
    @AccessLevelValid
    String accessLevel;

    @NotBlank
    @Pattern(regexp = "[A-Z][a-z]+")
    String name;

    @NotBlank
    @Pattern(regexp = "[A-Z][a-z]+")
    String surname;

    @Email
    @NotBlank
    @Size(max = 50)
    String email;

}
