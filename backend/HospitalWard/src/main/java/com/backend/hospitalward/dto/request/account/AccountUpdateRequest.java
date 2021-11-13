package com.backend.hospitalward.dto.request.account;

import com.backend.hospitalward.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
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
