package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.AccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.READ_COMMITTED, timeout = 3)
public interface AccessLevelRepository extends JpaRepository<AccessLevel, Long> {

    @Query("SELECT aL FROM AccessLevel aL, Account a WHERE a.id = aL.id AND a.login =:login")
    Optional<AccessLevel> findAccessLevelByLogin(String login);

    Optional<AccessLevel> findAccessLevelByName(String name);
}
