package com.backend.hospitalward.dto.response.account;

import com.backend.hospitalward.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountGeneralResponse extends BaseDTO {

    String login;

    String type;

    String accessLevel;

    String name;

    String surname;

    @Override
    public Long getSignablePayload() {
        return getVersion();
    }
}
