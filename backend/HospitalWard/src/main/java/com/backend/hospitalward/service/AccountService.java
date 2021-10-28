package com.backend.hospitalward.service;


import com.backend.hospitalward.exception.AccountException;
import com.backend.hospitalward.exception.MedicalStaffException;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.model.Specialization;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.SpecializationRepository;
import com.backend.hospitalward.security.SecurityConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class AccountService {

    AccountRepository accountRepository;

    AccessLevelRepository accessLevelRepository;

    SpecializationRepository specializationRepository;

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Account getAccountByLogin(String login) {
        return accountRepository.findAccountByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private void createBaseAccount(Account account, String accessLevel) {
        account.setVersion(0L);
        account.setPassword(Sha512DigestUtils.shaHex(account.getPassword()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName();

        account.setCreatedBy(accountRepository.findAccountByLogin(currentUser).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND)));

        String fullName = (account.getName() + "." + account.getSurname()).toLowerCase();
        int sameNameCount = accountRepository.findAccountsByLoginContains(fullName).size();

        if (sameNameCount == 0) {
            account.setLogin(fullName);
        } else {
            int addedNumber = sameNameCount + 1;
            account.setLogin(fullName + addedNumber);
        }

        account.setAccessLevel(accessLevelRepository.findAccessLevelByName(accessLevel).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND)));
    }

    public void createAccount(Account account, String accessLevel) {
        createBaseAccount(account, accessLevel);

        accountRepository.save(account);

    }

    public void createMedicalStaff(MedicalStaff medicalStaff, String accessLevel, List<String> specializations) {
        if ((medicalStaff.getLicenseNr().endsWith("P") && !accessLevel.equals(SecurityConstants.HEAD_NURSE)) ||
                (!medicalStaff.getLicenseNr().endsWith("P") && accessLevel.equals(SecurityConstants.HEAD_NURSE))) {
            throw MedicalStaffException.createBadRequestException(MedicalStaffException.LICENSE_NUMBER);
        }

        createBaseAccount(medicalStaff, accessLevel);

        List<Specialization> specializationsList = specializations.stream()
                .map(name -> specializationRepository.findSpecializationByName(name).orElseThrow(() ->
                        AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND)))
                .collect(Collectors.toList());

        medicalStaff.setSpecializations(specializationsList);
        accountRepository.save(medicalStaff);
    }
}
