package com.backend.hospitalward.service;


import com.backend.hospitalward.exception.AccessLevelException;
import com.backend.hospitalward.exception.AccountException;
import com.backend.hospitalward.exception.MedicalStaffException;
import com.backend.hospitalward.exception.UrlException;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.model.Specialization;
import com.backend.hospitalward.model.Url;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.SpecializationRepository;
import com.backend.hospitalward.repository.UrlRepository;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.model.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.security.enterprise.credential.Password;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class AccountService {

//    @Value("${url.expiration.time:86400}")
//    int secondsExpirationTime;

    AccountRepository accountRepository;

    AccessLevelRepository accessLevelRepository;

    SpecializationRepository specializationRepository;

    UrlRepository urlRepository;

    //region GET

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Account getAccountByLogin(String login) {
        return accountRepository.findAccountByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    //endregion

    //region CREATE


    private void createBaseAccount(Account account, String accessLevel, Account createdBy) {
//        if (urlRepository.findListByEmail(account.getEmail()).size() != 0) {
//            throw UrlException.createExceptionConflict(OneTimeUrlExceptions.NEW_EMAIL_UNIQUE);
//        }

        account.setVersion(0L);
        account.setPassword(Sha512DigestUtils.shaHex(account.getPassword()));

        account.setCreatedBy(createdBy);

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

    private void createUrl(Account account, Account director) {
        Url url = Url.builder()
                .codeDirector(RandomStringUtils.randomAlphanumeric(5))
                .codeEmployee(RandomStringUtils.randomAlphanumeric(5))
                .accountDirector(director)
                .accountEmployee(account)
                //.expirationDate(Timestamp.from(Instant.now().plus(secondsExpirationTime, SECONDS)))
                .creationDate(Timestamp.from(Instant.now()))
                .expirationDate(Timestamp.from(Instant.now().plus(86400, SECONDS)))
                .createdBy(director)
                .build();

        urlRepository.save(url);
    }

    public void createAccount(Account account, String accessLevel, String createdBy) {
        Account director = accountRepository.findAccountByLogin(createdBy).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND));

        createBaseAccount(account, accessLevel, director);

        accountRepository.save(account);

        createUrl(account, director);

        //TODO mail

    }

    public void createMedicalStaff(MedicalStaff medicalStaff, String accessLevel, List<String> specializations,
                                   String createdBy) {
        if ((medicalStaff.getLicenseNr().endsWith("P") && !accessLevel.equals(SecurityConstants.HEAD_NURSE)) ||
                (!medicalStaff.getLicenseNr().endsWith("P") && accessLevel.equals(SecurityConstants.HEAD_NURSE))) {
            throw MedicalStaffException.createBadRequestException(MedicalStaffException.LICENSE_NUMBER);
        }

        Account director = accountRepository.findAccountByLogin(createdBy).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND));

        createBaseAccount(medicalStaff, accessLevel, director);

        List<Specialization> specializationsList = specializations.stream()
                .map(name -> specializationRepository.findSpecializationByName(name).orElseThrow(() ->
                        AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND)))
                .collect(Collectors.toList());

        medicalStaff.setSpecializations(specializationsList);
        accountRepository.save(medicalStaff);

        createUrl(medicalStaff, director);
    }

    //endregion

    //region UPDATES

    public void changePassword(String login, Password oldPassword, Password newPassword) {
        Account account = accountRepository.findAccountByLogin(login).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND));
        String hashedOldPassword = Sha512DigestUtils.shaHex(String.valueOf(oldPassword.getValue()));

        if (!hashedOldPassword.equals(account.getPassword())) {
            throw AccountException.createBadRequestException(AccountException.PASSWORD_INCORRECT);
        }
        if (String.valueOf(newPassword.getValue()).equals(String.valueOf(oldPassword.getValue()))) {
            throw AccountException.createConflictException(AccountException.PASSWORD_THE_SAME);
        }

        account.setPassword(Sha512DigestUtils.shaHex(String.valueOf(newPassword.getValue())));
        account.setModificationDate(Timestamp.from(Instant.now()));
        accountRepository.save(account);

        //TODO mail
    }

    public void changeActivity(String login, boolean newActivity, String modifiedBy) {
        Account account = accountRepository.findAccountByLogin(login).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND));

        account.setActive(newActivity);
        account.setModificationDate(Timestamp.from(Instant.now()));
        if (modifiedBy == null || login.equals(modifiedBy)) {
            account.setModifiedBy(null);
        } else {
            account.setModifiedBy(accountRepository.findAccountByLogin(modifiedBy).orElseThrow(() ->
                    AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND)));
        }

        accountRepository.save(account);

        //TODO mail
    }

    private Account updateBaseAccount(Account account, String modifiedBy) {
        Account accountFromDB = accountRepository.findAccountByLogin(account.getLogin()).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND));

        accountFromDB.setVersion(account.getVersion());

        if (account.getName() != null && !account.getName().isEmpty()) {
            accountFromDB.setName(account.getName());
        }
        if (account.getSurname() != null && !account.getSurname().isEmpty()) {
            accountFromDB.setSurname(account.getSurname());
        }
        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            accountFromDB.setEmail(account.getEmail());
        }
        accountFromDB.setModificationDate(Timestamp.from(Instant.now()));

        if (modifiedBy.equals(account.getLogin())) {
            accountFromDB.setModifiedBy(null);
        } else {
            Account accModifiedBy = accountRepository.findAccountByLogin(modifiedBy).orElseThrow(() ->
                    AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND));
            accountFromDB.setModifiedBy(accModifiedBy);
        }

        return accountFromDB;
    }

    public void updateAccount(Account account, String modifiedBy) {

        accountRepository.save(updateBaseAccount(account, modifiedBy));

        //TODO mail
    }

    public void updateMedicalStaff(MedicalStaff account, String modifiedBy, List<String> specializations) {

        MedicalStaff modifiedMedicalStaff = (MedicalStaff) updateBaseAccount(account, modifiedBy);

        if (account.getLicenseNr() != null && !account.getLicenseNr().isEmpty()) {
            modifiedMedicalStaff.setLicenseNr(account.getLicenseNr());
        }
        if (account.getAcademicDegree() != null && !account.getAcademicDegree().isEmpty()) {
            modifiedMedicalStaff.setAcademicDegree(account.getAcademicDegree());
        }
        if (account.getSpecializations() != null && !account.getSpecializations().isEmpty()) {
            List<Specialization> specializationsList = specializations.stream()
                    .map(name -> specializationRepository.findSpecializationByName(name).orElseThrow(() ->
                            AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND)))
                    .collect(Collectors.toList());

            modifiedMedicalStaff.setSpecializations(specializationsList);
        } else {
            modifiedMedicalStaff.setSpecializations(Collections.emptyList());
        }

        accountRepository.save(modifiedMedicalStaff);

        //TODO mail
    }

    public void confirmAccount(String urlCode) {
        if (urlCode == null || urlCode.length() != 10) {
            throw UrlException.createNotFoundException(UrlException.URL_NOT_FOUND);
        }

        Url url = urlRepository.findUrlByCodeDirectorAndCodeEmployee(urlCode.substring(0, 5), urlCode.substring(5, 10))
                .orElseThrow(() -> UrlException.createNotFoundException(UrlException.URL_NOT_FOUND));

        if (Instant.now().isAfter(url.getExpirationDate().toInstant())) {
            throw UrlException.createGoneException(UrlException.URL_EXPIRED);
        }
//        else if (!url.getActionType().equals("verify")) {
//            throw AccountExceptions.createBadRequestException(AccountExceptions.ERROR_URL_TYPE);
//        }

        if (urlCode.equals(url.getCodeDirector() + url.getCodeEmployee())) {
            Account account = accountRepository.findAccountByLogin(url.getAccountEmployee().getLogin()).orElseThrow(() ->
                    AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND));
            account.setConfirmed(true);
            account.setModificationDate(Timestamp.from(Instant.now()));
            accountRepository.save(account);
            urlRepository.delete(url);
            return;
        }

        throw UrlException.createNotFoundException(UrlException.URL_NOT_FOUND);
    }

    public void changeAccessLevel(String newAccessLevel, String login, String modifiedBy) {

        Account account = accountRepository.findAccountByLogin(login).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND));

        if (account.getType().equals("OFFICE")) {
            throw AccessLevelException.createConflictException(AccessLevelException.OFFICE_STAFF_ACCESS_LEVEL_CHANGE);
        }
        if (newAccessLevel.equals("SECRETARY")) {
            throw AccessLevelException.createConflictException(AccessLevelException.MEDICAL_STAFF_TO_OFFICE_CHANGE);
        }
        if (cannotChangeAccessLevel(newAccessLevel, account, "TREATMENT DIRECTOR")) {
            throw AccessLevelException.createConflictException(AccessLevelException.TREATMENT_DIRECTOR_REQUIRED);
        }
        if (cannotChangeAccessLevel(newAccessLevel, account, "HEAD NURSE")) {
            throw  AccessLevelException.createConflictException(AccessLevelException.HEAD_NURSE_REQUIRED);
        }

        AccessLevel accessLevel = accessLevelRepository.findAccessLevelByName(newAccessLevel).orElseThrow(() ->
                AccessLevelException.createNotFoundException(AccessLevelException.ACCESS_LEVEL_NOT_FOUND));

        account.setAccessLevel(accessLevel);
        account.setModificationDate(Timestamp.from(Instant.now()));
        account.setModifiedBy(accountRepository.findAccountByLogin(modifiedBy).orElseThrow(() ->
                AccountException.createNotFoundException(AccountException.ACCOUNT_NOT_FOUND)));

        accountRepository.save(account);

    }

    private boolean cannotChangeAccessLevel(String newAccessLevel, Account account, String s) {
        return accountRepository.findAccountsByAccessLevel_Name(s).size() == 1 &&
                account.getAccessLevel().getName().equals(s) &&
                !newAccessLevel.equals(s);
    }

    //endregion
}
