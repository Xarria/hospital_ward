package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.request.disease.DiseaseCreateRequest;
import com.backend.hospitalward.dto.request.disease.DiseaseUpdateRequest;
import com.backend.hospitalward.dto.response.disease.DiseaseDetailsResponse;
import com.backend.hospitalward.dto.response.disease.DiseaseGeneralResponse;
import com.backend.hospitalward.model.Disease;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface DiseaseMapper {

    @Named("mapModifiedBy")
    default String mapModifiedBy(Disease disease) {
        if (disease.getModifiedBy() != null) {
            return disease.getModifiedBy().getLogin();
        }
        return null;
    }

    @Named("mapCreatedBy")
    default String mapCreatedBy(Disease disease) {
        if (disease.getCreatedBy() != null) {
            return disease.getCreatedBy().getLogin();
        }
        return null;
    }

    @Mapping(target = "patients", ignore = true)
    Disease toDisease(DiseaseCreateRequest diseaseCreateRequest);

    @Mapping(target = "patients", ignore = true)
    Disease toDisease(DiseaseUpdateRequest diseaseUpdateRequest);

    DiseaseGeneralResponse toDiseaseGeneralResponse(Disease disease);

    @Mapping(target = "createdBy", source = "disease", qualifiedByName = "mapCreatedBy")
    @Mapping(target = "modifiedBy", source = "disease", qualifiedByName = "mapModifiedBy")
    DiseaseDetailsResponse toDiseaseDetailsResponse(Disease disease);

}
