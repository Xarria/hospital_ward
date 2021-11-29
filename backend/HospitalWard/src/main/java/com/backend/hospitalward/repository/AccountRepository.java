package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.READ_COMMITTED, timeout = 3)
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findAccountByLogin(String login);

    List<Account> findAccountsByLoginContains(String fullName);

    List<Account> findAccountsByAccessLevel_Name(String accessLevelName);

    List<Account> findAccountsByEmail(String email);

    Optional<Account> findAccountByEmailAndConfirmedIsTrue(String email);

    Optional<Account> findAccountByNameAndSurname(String name, String surname);

     @Query(value = "SELECT a FROM Account WHERE a.confirmed = false " +
             "AND time_to_sec(timediff(a.creation_date, now())) < - removalTime", nativeQuery = true)
    List<Account> findAccountsByUnconfirmedAndExpired(long removalTime);
}
