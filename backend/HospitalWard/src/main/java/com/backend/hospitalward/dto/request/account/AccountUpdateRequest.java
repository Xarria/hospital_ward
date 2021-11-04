package com.backend.hospitalward.dto.request.account;

import com.backend.hospitalward.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;


@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountUpdateRequest extends BaseDTO {

    String login;

    String name;

    String surname;

    String email;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
