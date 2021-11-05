package com.backend.hospitalward.dto;

import com.backend.hospitalward.util.etag.SignableDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.json.bind.annotation.JsonbTransient;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseDTO implements SignableDTO {

    Long version;
}
