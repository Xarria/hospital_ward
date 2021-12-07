package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import com.backend.hospitalward.dto.response.queue.QueueResponse;
import com.backend.hospitalward.model.Disease;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.Queue;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface QueueMapper {

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

    QueueResponse toQueueResponse(Queue queue);

    @Mapping(target = "patientType", qualifiedByName = "mapPatientType")
    @Mapping(target = "status", qualifiedByName = "mapPatientStatus")
    @Mapping(target = "mainDoctor", qualifiedByName = "mapMainDoctor")
    @Mapping(target = "cathererRequired", qualifiedByName = "mapCathererRequired")
    @Mapping(target = "surgeryRequired", qualifiedByName = "mapSurgeryRequired")
    List<PatientGeneralResponse> toPatientGeneralResponse(List<Patient> patients);
}
