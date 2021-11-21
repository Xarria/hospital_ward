package com.backend.hospitalward.dto;

import com.backend.hospitalward.util.etag.SignableDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseDTO implements SignableDTO {

    @NotNull
    @PositiveOrZero
    Long version;
}
