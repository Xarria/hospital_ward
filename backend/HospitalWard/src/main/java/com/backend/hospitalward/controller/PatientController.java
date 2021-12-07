package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.patient.PatientCreateRequest;
import com.backend.hospitalward.dto.request.patient.PatientUpdateRequest;
import com.backend.hospitalward.dto.response.patient.PatientDetailsResponse;
import com.backend.hospitalward.dto.response.patient.PatientGeneralResponse;
import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.PreconditionFailedException;
import com.backend.hospitalward.mapper.PatientMapper;
import com.backend.hospitalward.security.annotation.Authenticated;
import com.backend.hospitalward.security.annotation.MedicAuthorities;
import com.backend.hospitalward.security.annotation.TreatmentDirectorAuthority;
import com.backend.hospitalward.service.PatientService;
import com.backend.hospitalward.util.etag.ETagValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
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
            throw new BadRequestException(ErrorKey.NO_PERMISSION_TO_CREATE_URGENT_PATIENT);
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
    @PutMapping
    public ResponseEntity<?> updatePatient(@RequestBody @Valid PatientUpdateRequest patientUpdateRequest,
                                           @CurrentSecurityContext SecurityContext securityContext,
                                           @RequestHeader("If-Match") String eTag) {

        checkETagHeader(patientUpdateRequest, eTag);

        patientService.updatePatient(patientMapper.toPatient(patientUpdateRequest), patientUpdateRequest.getDiseases(),
                patientUpdateRequest.getMainDoctor(), patientUpdateRequest.getCovidStatus(),
                securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @MedicAuthorities
    @PutMapping("/confirm/{id}")
    public ResponseEntity<?> confirmPatient(@PathVariable("id") long id, @RequestBody LocalDate queueDate) {

        patientService.confirmPatient(id, queueDate);

        return ResponseEntity.ok().build();
    }

    @Authenticated
    @PutMapping("/date/{id}")
    public ResponseEntity<?> changeAdmissionDate(@PathVariable("id") long id, @RequestBody LocalDate admissionDate,
                                                 @CurrentSecurityContext SecurityContext securityContext) {

        patientService.changePatientAdmissionDate(id, admissionDate, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @PutMapping("/urgency/{id}")
    public ResponseEntity<?> changeUrgency(@PathVariable("id") long id, @RequestBody boolean urgent,
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
