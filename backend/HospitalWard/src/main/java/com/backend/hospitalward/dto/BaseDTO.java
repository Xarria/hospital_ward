package com.backend.hospitalward.dto;

import com.backend.hospitalward.util.etag.SignableDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseDTO implements SignableDTO {

    Long version;
}
