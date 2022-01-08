package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.patient.PatientCreateRequest;
import com.backend.hospitalward.dto.request.patient.PatientUpdateRequest;
import com.backend.hospitalward.dto.response.patient.PatientDetailsResponse;
import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.PreconditionFailedException;
import com.backend.hospitalward.mapper.PatientMapper;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.security.annotation.Authenticated;
import com.backend.hospitalward.security.annotation.MedicAuthorities;
import com.backend.hospitalward.security.annotation.TreatmentDirectorAuthority;
import com.backend.hospitalward.service.PatientService;
import com.backend.hospitalward.util.etag.ETagValidator;
import com.backend.hospitalward.util.serialization.LocalDateJsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.ejb.Local;
import javax.validation.Valid;
import javax.ws.rs.ForbiddenException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    PatientService patientService;

    PatientMapper patientMapper;

    @Authenticated
    @GetMapping
    public ResponseEntity<List<PatientGeneralResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients().stream()
                .map(patientMapper::toPatientGeneralResponse)
                .collect(Collectors.toList()));
    }

    @Authenticated
    @GetMapping("/{id}")
    public ResponseEntity<PatientDetailsResponse> getPatientById(@PathVariable("id") long id) {
        PatientDetailsResponse patient = patientMapper.toPatientDetailsResponse(patientService.getPatientById(id));
        return ResponseEntity.ok()
                .eTag(ETagValidator.calculateDTOSignature(patient))
                .body(patient);
    }

    @Authenticated
    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody @Valid PatientCreateRequest patientCreateRequest,
                                           @CurrentSecurityContext SecurityContext securityContext) {
        if (patientCreateRequest.isUrgent()) {
            throw new ForbiddenException(ErrorKey.NO_PERMISSION_TO_CREATE_URGENT_PATIENT);
        }

        patientService.createPatient(patientMapper.toPatient(patientCreateRequest),
                securityContext.getAuthentication().getName(), patientCreateRequest.getDiseases(),
                patientCreateRequest.getMainDoctor(), patientCreateRequest.getCovidStatus());

        return ResponseEntity.ok().build();
    }

    @MedicAuthorities
    @PostMapping("/urgent")
    public ResponseEntity<?> createUrgentPatient(@RequestBody @Valid PatientCreateRequest patientCreateRequest,
                                                 @CurrentSecurityContext SecurityContext securityContext) {
        if (!patientCreateRequest.isUrgent()) {
            throw new BadRequestException(ErrorKey.PATIENT_NOT_URGENT);
        }

        patientService.createPatient(patientMapper.toPatient(patientCreateRequest),
                securityContext.getAuthentication().getName(), patientCreateRequest.getDiseases(),
                patientCreateRequest.getMainDoctor(), patientCreateRequest.getCovidStatus());

        return ResponseEntity.ok().build();
    }

    @Authenticated
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable("id") long id,
                                           @RequestBody @Valid PatientUpdateRequest patientUpdateRequest,
                                           @CurrentSecurityContext SecurityContext securityContext,
                                           @RequestHeader("If-Match") String eTag) {

        checkETagHeader(patientUpdateRequest, eTag);

        Patient patient = patientMapper.toPatient(patientUpdateRequest);

        patientService.updatePatient(id, patient, getDiseases(patientUpdateRequest),
                getMainDoctor(patientUpdateRequest), getCovidStatus(patientUpdateRequest),
                securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    private List<String> getDiseases(PatientUpdateRequest patientUpdateRequest) {
        return patientUpdateRequest.getDiseases() != null ? patientUpdateRequest.getDiseases() : Collections.emptyList();
    }

    private String getMainDoctor(PatientUpdateRequest patientUpdateRequest) {
        return patientUpdateRequest.getMainDoctor() != null ? patientUpdateRequest.getMainDoctor() : "";
    }

    private String getCovidStatus(PatientUpdateRequest patientUpdateRequest) {
        return patientUpdateRequest.getCovidStatus() != null ? patientUpdateRequest.getCovidStatus() : "";
    }

    @MedicAuthorities
    @GetMapping("/confirm/{id}")
    public ResponseEntity<?> confirmPatient(@PathVariable("id") long id) {

        patientService.confirmPatient(id);

        return ResponseEntity.ok().build();
    }

    @Authenticated
    @GetMapping("/date/{id}/{date}")
    public ResponseEntity<?> changeAdmissionDate(@PathVariable("id") long id, @PathVariable("date")
                                                @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate admissionDate,
                                                 @CurrentSecurityContext SecurityContext securityContext) {

        patientService.changePatientAdmissionDate(id, admissionDate, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @GetMapping("/urgency/{id}/{newValue}")
    public ResponseEntity<?> changeUrgency(@PathVariable("id") long id, @PathVariable("newValue") boolean urgent,
                                           @CurrentSecurityContext SecurityContext securityContext) {

        patientService.changePatientUrgency(id, urgent, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable("id") long id) {

        patientService.deletePatient(id);

        return ResponseEntity.ok().build();
    }

    private void checkETagHeader(@RequestBody @Valid PatientUpdateRequest patientUpdateRequest,
                                 @RequestHeader("If-Match") String eTag) {

        if (patientUpdateRequest.getName() == null || patientUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, patientUpdateRequest)) {
            throw new PreconditionFailedException(ErrorKey.ETAG_INVALID);
        }
    }
}
