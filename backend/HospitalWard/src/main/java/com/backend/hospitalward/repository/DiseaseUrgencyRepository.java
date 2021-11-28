package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.DiseaseUrgency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseUrgencyRepository extends JpaRepository<DiseaseUrgency, Long> {

    Optional<DiseaseUrgency> findDiseaseUrgencyByUrgency(String urgency);
}
