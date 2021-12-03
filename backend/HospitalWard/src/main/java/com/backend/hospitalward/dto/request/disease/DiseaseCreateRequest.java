package com.backend.hospitalward.dto.request.disease;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DiseaseCreateRequest {

    @NotBlank
    String name;

    @NotNull
    boolean cathererRequired;

    @NotNull
    boolean surgeryRequired;
}
