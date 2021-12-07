package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.request.patient.PatientCreateRequest;
import com.backend.hospitalward.dto.request.patient.PatientUpdateRequest;
import com.backend.hospitalward.dto.response.disease.DiseaseGeneralResponse;
import com.backend.hospitalward.dto.response.patient.PatientDetailsResponse;
import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.Disease;
import com.backend.hospitalward.model.Patient;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PatientMapper {

    @Named("mapPatientType")
    default String mapPatientType(Patient patient) {
        if (patient.getPatientType() != null) {
            return patient.getPatientType().getName();
        }
        return null;
    }

    @Named("mapPatientStatus")
    default String mapPatientStatus(Patient patient) {
        if (patient.getStatus() != null) {
            return patient.getStatus().getName();
        }
        return null;
    }

    @Named("mapCovidStatus")
    default String mapCovidStatus(Patient patient) {
        if (patient.getCovidStatus() != null) {
            return patient.getCovidStatus().getStatus();
        }
        return null;
    }

    @Named("mapCathererRequired")
    default boolean mapCathererRequired(Patient patient) {
        if (patient.getDiseases() != null && !patient.getDiseases().isEmpty()) {
            return patient.getDiseases().stream().anyMatch(Disease::isCathererRequired);
        }
        return false;
    }

    @Named("mapSurgeryRequired")
    default boolean mapSurgeryRequired(Patient patient) {
        if (patient.getDiseases() != null && !patient.getDiseases().isEmpty()) {
            return patient.getDiseases().stream().anyMatch(Disease::isSurgeryRequired);
        }
        return false;
    }

    @Named("mapMainDoctor")
    default String mapMainDoctor(Patient patient) {
        if (patient.getMainDoctor() != null) {
            return patient.getMainDoctor().getLogin();
        }
        return null;
    }

    @Named("mapModifiedBy")
    default String mapModifiedBy(Account account) {
        if (account.getModifiedBy() != null) {
            return account.getModifiedBy().getName();
        }
        return null;
    }

    @Named("mapCreatedBy")
    default String mapCreatedBy(Account account) {
        if (account.getCreatedBy() != null) {
            return account.getCreatedBy().getName();
        }
        return null;
    }

    @Mapping(target = "patientType", qualifiedByName = "mapPatientType")
    @Mapping(target = "status", qualifiedByName = "mapPatientStatus")
    @Mapping(target = "mainDoctor", qualifiedByName = "mapMainDoctor")
    @Mapping(target = "cathererRequired", qualifiedByName = "mapCathererRequired")
    @Mapping(target = "surgeryRequired", qualifiedByName = "mapSurgeryRequired")
    PatientGeneralResponse toPatientGeneralResponse(Patient patient);

    @Mapping(target = "covidStatus", qualifiedByName = "mapCovidStatus")
    @Mapping(target = "patientType", qualifiedByName = "mapPatientType")
    @Mapping(target = "status", qualifiedByName = "mapPatientStatus")
    @Mapping(target = "mainDoctor", qualifiedByName = "mapMainDoctor")
    @Mapping(target = "createdBy", qualifiedByName = "mapCreatedBy")
    @Mapping(target = "modifiedBy", qualifiedByName = "mapModifiedBy")
    PatientDetailsResponse toPatientDetailsResponse(Patient patient);

    DiseaseGeneralResponse toDiseaseGeneralResponse(Disease disease);

    @Mapping(target = "covidStatus", ignore = true)
    @Mapping(target = "patientType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "mainDoctor", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Patient toPatient(PatientCreateRequest patientCreateRequest);

    @Mapping(target = "covidStatus", ignore = true)
    @Mapping(target = "patientType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "mainDoctor", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Patient toPatient(PatientUpdateRequest patientUpdateRequest);


}
