package com.backend.hospitalward.dto.request.account;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {

    @NotBlank
    @Size(min = 8)
    String password;

    @NotBlank
    String accessLevel;

    @NotBlank
    String name;

    @NotBlank
    String surname;

    @NotBlank
    String email;

}
