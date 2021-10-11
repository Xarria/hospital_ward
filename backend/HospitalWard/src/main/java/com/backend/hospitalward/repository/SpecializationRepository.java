package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    Optional<Specialization> findSpecializationByName(String name);
}
