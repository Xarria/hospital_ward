package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.disease.DiseaseCreateRequest;
import com.backend.hospitalward.dto.request.disease.DiseaseUpdateRequest;
import com.backend.hospitalward.dto.response.disease.DiseaseDetailsResponse;
import com.backend.hospitalward.dto.response.disease.DiseaseGeneralResponse;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.PreconditionFailedException;
import com.backend.hospitalward.mapper.DiseaseMapper;
import com.backend.hospitalward.security.annotation.DoctorOrTreatmentDirectorAuthority;
import com.backend.hospitalward.security.annotation.MedicAuthorities;
import com.backend.hospitalward.security.annotation.TreatmentDirectorAuthority;
import com.backend.hospitalward.service.DiseaseService;
import com.backend.hospitalward.util.etag.ETagValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    DiseaseService diseaseService;

    DiseaseMapper diseaseMapper;

    @MedicAuthorities
    @GetMapping
    public ResponseEntity<List<DiseaseGeneralResponse>> getAllDiseases() {
        return ResponseEntity.ok(diseaseService.getAllDiseases().stream()
                .map(diseaseMapper::toDiseaseGeneralResponse)
                .collect(Collectors.toList()));
    }

    @MedicAuthorities
    @GetMapping(path = "/{name}")
    public ResponseEntity<DiseaseDetailsResponse> getDiseaseByName(@PathVariable("name") String name) {
        DiseaseDetailsResponse disease = diseaseMapper.toDiseaseDetailsResponse(diseaseService.getDiseaseByName(name));
        return ResponseEntity.ok()
                .eTag(ETagValidator.calculateDTOSignature(disease))
                .body(disease);
    }

    @DoctorOrTreatmentDirectorAuthority
    @PostMapping
    public ResponseEntity<?> createDisease(@CurrentSecurityContext SecurityContext securityContext,
                                           @RequestBody @Valid DiseaseCreateRequest diseaseCreateRequest) {
        diseaseService.createDisease(diseaseMapper.toDisease(diseaseCreateRequest),
                securityContext.getAuthentication().getName(), diseaseCreateRequest.getUrgency());

        return ResponseEntity.ok().build();
    }

    @DoctorOrTreatmentDirectorAuthority
    @PutMapping(path = "/{name}")
    public ResponseEntity<?> updateDisease(@CurrentSecurityContext SecurityContext securityContext,
                                           @RequestHeader("If-Match") String eTag,
                                           @RequestBody @Valid DiseaseUpdateRequest diseaseUpdateRequest) {

        checkETagHeader(diseaseUpdateRequest, eTag);

        diseaseService.updateDisease(diseaseMapper.toDisease(diseaseUpdateRequest),
                securityContext.getAuthentication().getName(), diseaseUpdateRequest.getUrgency());

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @DeleteMapping(path = "/{name}")
    public ResponseEntity<?> deleteDisease(@PathVariable("name") String name) {

        diseaseService.deleteDisease(name);

        return ResponseEntity.ok().build();
    }

    private void checkETagHeader(@RequestBody @Valid DiseaseUpdateRequest diseaseUpdateRequest,
                                 @RequestHeader("If-Match") String eTag) {

        if (diseaseUpdateRequest.getName() == null || diseaseUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, diseaseUpdateRequest)) {
            throw new PreconditionFailedException(ErrorKey.ETAG_INVALID);
        }
    }

}
