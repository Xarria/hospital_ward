package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import com.backend.hospitalward.dto.response.queue.QueueResponse;
import com.backend.hospitalward.model.*;
import org.mapstruct.*;

import javax.ws.rs.core.Link;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
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

    @Named("mapWaitingPatients")
    default List<PatientGeneralResponse> mapWaitingPatients(Queue queue) {
        List<Patient> waitingPatients = queue.getWaitingPatients();
        List<PatientGeneralResponse> patientGeneralResponseList = new LinkedList<>();

        for(Patient patient: waitingPatients) {
            patientGeneralResponseList.add(toPatientGeneralResponse(patient));
        }
        return patientGeneralResponseList;
    }

    @Named("mapConfirmedPatients")
    default List<PatientGeneralResponse> mapConfirmedPatients(Queue queue) {
        List<Patient> confirmedPatients = queue.getConfirmedPatients();
        List<PatientGeneralResponse> patientGeneralResponseList = new LinkedList<>();

        for(Patient patient: confirmedPatients) {
            patientGeneralResponseList.add(toPatientGeneralResponse(patient));
        }
        return patientGeneralResponseList;
    }

    @Mapping(target = "patientsWaiting", qualifiedByName = "mapWaitingPatients", source = "queue")
    @Mapping(target = "patientsConfirmed", qualifiedByName = "mapConfirmedPatients", source = "queue")
    QueueResponse toQueueResponse(Queue queue);


    @Mapping(target = "patientType", qualifiedByName = "mapPatientType")
    @Mapping(target = "status", qualifiedByName = "mapPatientStatus")
    @Mapping(target = "cathererRequired", source = "patient", qualifiedByName = "mapCathererRequired")
    @Mapping(target = "surgeryRequired", source = "patient", qualifiedByName = "mapSurgeryRequired")
    PatientGeneralResponse toPatientGeneralResponse(Patient patient);
}
