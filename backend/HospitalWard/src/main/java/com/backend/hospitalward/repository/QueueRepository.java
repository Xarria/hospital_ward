package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.READ_COMMITTED, timeout = 3)
public interface QueueRepository extends JpaRepository<Queue, Long> {

    Optional<Queue> findQueueByDate(LocalDate date);

    List<Queue> findQueuesByLockedFalseAndDateAfter(LocalDate date);

    List<Queue> findQueuesByLockedTrueAndDateAfter(LocalDate date);

    Optional<Queue> findQueueByPatientsWaitingContains(Patient patient);

    List<Queue> findQueuesByDateAfter(LocalDate date);

    List<Queue> findQueuesByDateBefore(LocalDate date);
}
