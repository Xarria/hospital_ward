package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.request.disease.DiseaseCreateRequest;
import com.backend.hospitalward.dto.request.disease.DiseaseUpdateRequest;
import com.backend.hospitalward.dto.response.disease.DiseaseResponse;
import com.backend.hospitalward.model.Disease;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface DiseaseMapper {

    @Mapping(target = "patients", ignore = true)
    Disease toDisease(DiseaseCreateRequest diseaseCreateRequest);

    @Mapping(target = "patients", ignore = true)
    Disease toDisease(DiseaseUpdateRequest diseaseUpdateRequest);

    DiseaseResponse toDiseaseResponse(Disease disease);
}
