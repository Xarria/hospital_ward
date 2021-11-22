package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.request.disease.DiseaseCreateRequest;
import com.backend.hospitalward.dto.request.disease.DiseaseUpdateRequest;
import com.backend.hospitalward.dto.response.disease.DiseaseDetailsResponse;
import com.backend.hospitalward.dto.response.disease.DiseaseGeneralResponse;
import com.backend.hospitalward.model.Disease;
import org.mapstruct.Mapping;

public interface DiseaseMapper extends BaseMapper{

    @Mapping(target = "patients", ignore = true)
    Disease toDisease(DiseaseCreateRequest diseaseCreateRequest);

    @Mapping(target = "patients", ignore = true)
    Disease toDisease(DiseaseUpdateRequest diseaseUpdateRequest);

    DiseaseGeneralResponse toDiseaseGeneralResponse(Disease disease);

    @Mapping(target = "createdBy", source = "medicalStaff", qualifiedByName = "mapCreatedBy")
    @Mapping(target = "modifiedBy", source = "medicalStaff", qualifiedByName = "mapModifiedBy")
    DiseaseDetailsResponse toDiseaseDetailsResponse(Disease disease);
}
