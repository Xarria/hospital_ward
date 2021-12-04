package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.READ_COMMITTED, timeout = 3)
public interface QueueRepository extends JpaRepository<Queue, Long> {

    Optional<Queue> findQueueByDate(Date date);

    List<Queue> findQueuesByLockedFalse();

    List<Queue> findQueuesByLockedTrue();

    Optional<Queue> findQueueByPatientsWaitingContains(Patient patient);

    List<Queue> findQueuesByDateAfter(Date date);

    List<Queue> findQueuesByDateBefore(Date date);
}
