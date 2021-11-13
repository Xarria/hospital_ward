package com.backend.hospitalward.dto;

import com.backend.hospitalward.util.etag.SignableDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.json.bind.annotation.JsonbTransient;

@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseDTO implements SignableDTO {

    Long version;
}
