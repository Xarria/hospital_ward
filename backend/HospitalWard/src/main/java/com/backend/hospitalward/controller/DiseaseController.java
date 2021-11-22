package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.response.disease.DiseaseResponse;
import com.backend.hospitalward.mapper.DiseaseMapper;
import com.backend.hospitalward.security.annotation.MedicAuthorities;
import com.backend.hospitalward.service.DiseaseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<DiseaseResponse>> getAllDiseases(){
        return ResponseEntity.ok(diseaseService.getAllDiseases().stream()
                .map(diseaseMapper::toDiseaseResponse)
                .collect(Collectors.toList()));
    }
}
