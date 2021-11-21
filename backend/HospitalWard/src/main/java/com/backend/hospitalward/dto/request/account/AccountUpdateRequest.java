package com.backend.hospitalward.dto.request.account;

import com.backend.hospitalward.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountUpdateRequest extends BaseDTO {

    @NotBlank
    @Pattern(regexp = "[a-z]+[.][a-z]+[2-9]*")
    String login;

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

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
