package com.backend.hospitalward.dto.request.account;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank
    @Size(min = 8)
    String oldPassword;

    @NotBlank
    @Size(min = 8)
    String newPassword;

}
