package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.request.patient.PatientCreateRequest;
import com.backend.hospitalward.dto.request.patient.PatientUpdateRequest;
import com.backend.hospitalward.dto.response.disease.DiseaseGeneralResponse;
import com.backend.hospitalward.dto.response.patient.PatientDetailsResponse;
import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import com.backend.hospitalward.model.*;
import com.backend.hospitalward.security.annotation.MedicAuthorities;
import org.mapstruct.*;

import java.sql.Date;
import java.time.LocalDate;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PatientMapper {

    @Named("mapPatientType")
    default String mapPatientType(PatientType patientType) {
        if (patientType != null) {
            return patientType.getName();
        }
        return null;
    }

    @Named("mapPatientStatus")
    default String mapPatientStatus(PatientStatus patientStatus) {
        if (patientStatus != null) {
            return patientStatus.getName();
        }
        return null;
    }

    @Named("mapCovidStatus")
    default String mapCovidStatus(CovidStatus covidStatus) {
        if (covidStatus != null) {
            return covidStatus.getStatus();
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
    default String mapMainDoctor(Account medicalStaff) {
        if (medicalStaff != null) {
            return medicalStaff.getLogin();
        }
        return null;
    }

    @Named("mapModifiedBy")
    default String mapModifiedBy(Account account) {
        if (account != null) {
            return account.getLogin();
        }
        return null;
    }

    @Named("mapCreatedBy")
    default String mapCreatedBy(Account account) {
        if (account != null) {
            return account.getLogin();
        }
        return null;
    }
    @Mapping(target = "patientType", qualifiedByName = "mapPatientType")
    @Mapping(target = "status", qualifiedByName = "mapPatientStatus")
    @Mapping(target = "mainDoctor", qualifiedByName = "mapMainDoctor")
    @Mapping(target = "cathererRequired", source = "patient", qualifiedByName = "mapCathererRequired")
    @Mapping(target = "surgeryRequired", source = "patient", qualifiedByName = "mapSurgeryRequired")
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
    @Mapping(target = "diseases", ignore = true)
    Patient toPatient(PatientCreateRequest patientCreateRequest);

    @Mapping(target = "covidStatus", ignore = true)
    @Mapping(target = "patientType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "mainDoctor", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "diseases", ignore = true)
    Patient toPatient(PatientUpdateRequest patientUpdateRequest);


}
