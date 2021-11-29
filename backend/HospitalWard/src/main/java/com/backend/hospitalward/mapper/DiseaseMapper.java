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
            return disease.getModifiedBy().getName();
        }
        return null;
    }

    @Named("mapCreatedBy")
    default String mapCreatedBy(Disease disease) {
        if (disease.getCreatedBy() != null) {
            return disease.getCreatedBy().getName();
        }
        return null;
    }

    @Named("mapUrgency")
    default String mapUrgency(Disease disease) {
        if (disease.getUrgency() != null) {
            return disease.getUrgency().getUrgency();
        }
        return null;
    }

    @Mapping(target = "urgency", ignore = true)
    @Mapping(target = "patients", ignore = true)
    Disease toDisease(DiseaseCreateRequest diseaseCreateRequest);

    @Mapping(target = "urgency", ignore = true)
    @Mapping(target = "patients", ignore = true)
    Disease toDisease(DiseaseUpdateRequest diseaseUpdateRequest);

    @Mapping(target = "urgency", source = "disease", qualifiedByName = "mapUrgency")
    DiseaseGeneralResponse toDiseaseGeneralResponse(Disease disease);

    @Mapping(target = "urgency", source = "disease", qualifiedByName = "mapUrgency")
    @Mapping(target = "createdBy", source = "disease", qualifiedByName = "mapCreatedBy")
    @Mapping(target = "modifiedBy", source = "disease", qualifiedByName = "mapModifiedBy")
    DiseaseDetailsResponse toDiseaseDetailsResponse(Disease disease);
}
