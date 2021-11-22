package com.backend.hospitalward.dto.response.disease;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DiseaseGeneralResponse {

    String name;

    boolean urgent;

    boolean cathererRequired;

    boolean surgeryRequired;
}
