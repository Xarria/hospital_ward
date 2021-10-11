package com.backend.hospitalward.repository;

import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findAccountByLogin(String login);

    List<Account> findAccountsByLoginContains(String fullName);

}
