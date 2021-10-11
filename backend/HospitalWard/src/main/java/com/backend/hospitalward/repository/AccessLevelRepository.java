package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.AccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessLevelRepository extends JpaRepository<AccessLevel, Long> {

    @Query("SELECT aL FROM AccessLevel aL, Account a WHERE a.id = aL.id AND a.login =:login")
    Optional<AccessLevel> findAccessLevelByLogin(String login);

    Optional<AccessLevel> findAccessLevelByName(String name);
}
