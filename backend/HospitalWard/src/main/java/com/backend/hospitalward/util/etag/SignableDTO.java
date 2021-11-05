package com.backend.hospitalward.util.etag;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface SignableDTO {

    @JsonIgnore
    Long getSignablePayload();
}
