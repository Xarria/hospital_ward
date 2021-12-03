package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.PatientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.READ_COMMITTED, timeout = 3)
public interface PatientTypeRepository extends JpaRepository<PatientType, Long> {

    Optional<PatientType> findPatientTypeByName(String name);
}
