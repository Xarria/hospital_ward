package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import com.backend.hospitalward.dto.response.queue.QueueResponse;
import com.backend.hospitalward.model.*;
import org.mapstruct.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface QueueMapper {

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

    @Named("mapDateToLocalDate")
    default LocalDate mapAdmissionDateToLocal(Date date) {
        if (date != null) {
            return date.toLocalDate();
        }
        return null;
    }

    @Named("mapLocalDateToDate")
    default Date mapAdmissionDateToDate(LocalDate localDate) {
        if (localDate != null) {
            return Date.valueOf(localDate);
        }
        return null;
    }

    QueueResponse toQueueResponse(Queue queue);

    @Mapping(target = "patientType", qualifiedByName = "mapPatientType")
    @Mapping(target = "status", qualifiedByName = "mapPatientStatus")
    @Mapping(target = "mainDoctor", qualifiedByName = "mapMainDoctor")
    @Mapping(target = "cathererRequired", source = "patient", qualifiedByName = "mapCathererRequired")
    @Mapping(target = "surgeryRequired", source = "patient", qualifiedByName = "mapSurgeryRequired")
    @Mapping(target = "admissionDate", qualifiedByName = "mapDateToLocalDate")
    @Mapping(target = "referralDate", qualifiedByName = "mapDateToLocalDate")
    PatientGeneralResponse toPatientGeneralResponse(Patient patient);
}
