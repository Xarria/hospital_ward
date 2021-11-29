package com.backend.hospitalward.service;


import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.*;
import com.backend.hospitalward.model.common.AccessLevelName;
import com.backend.hospitalward.model.common.AccountType;
import com.backend.hospitalward.model.common.UrlActionType;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.SpecializationRepository;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.util.notification.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import javax.security.enterprise.credential.Password;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Retryable(value = {PersistenceException.class, HibernateException.class, JDBCException.class},
        exclude = ConstraintViolationException.class, backoff = @Backoff(delay = 1000))
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class AccountService {

    AccountRepository accountRepository;

    AccessLevelRepository accessLevelRepository;

    SpecializationRepository specializationRepository;

    UrlService urlService;

    EmailSender emailSender;

    //region GET

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Account getAccountByLogin(String login) {
        return accountRepository.findAccountByLogin(login).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
    }

    //endregion

    //region CREATE

    private void createBaseAccount(Account account, String accessLevel, Account createdBy) {

        account.setVersion(0L);
        account.setCreatedBy(createdBy);

        String fullName = (account.getName() + "." + account.getSurname()).toLowerCase();
        int sameNameCount = accountRepository.findAccountsByLoginContains(fullName).size();

        if (sameNameCount == 0) {
            account.setLogin(fullName);
        } else {
            int addedNumber = sameNameCount + 1;
            account.setLogin(fullName + addedNumber);
        }

        if (account instanceof MedicalStaff && accessLevel.equals(AccessLevelName.SECRETARY)) {
            throw new BadRequestException(ErrorKey.ACCESS_LEVEL_INVALID_MEDIC);
        }
        if (!(account instanceof MedicalStaff) && !accessLevel.equals(AccessLevelName.SECRETARY)) {
            throw new BadRequestException(ErrorKey.ACCESS_LEVEL_INVALID_OFFICE);
        }

        account.setAccessLevel(accessLevelRepository.findAccessLevelByName(accessLevel).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCESS_LEVEL_NOT_FOUND)));

    }

    public void createAccount(Account account, String accessLevel, String createdBy) {
        Account director = accountRepository.findAccountByLogin(createdBy).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        createBaseAccount(account, accessLevel, director);

        accountRepository.save(account);

        urlService.createConfirmUrl(account, director);

    }

    public void createMedicalStaff(MedicalStaff medicalStaff, String accessLevel, List<String> specializations,
                                   String createdBy) {
        if ((medicalStaff.getLicenseNr().endsWith("P") && !accessLevel.equals(SecurityConstants.HEAD_NURSE)) ||
                (!medicalStaff.getLicenseNr().endsWith("P") && accessLevel.equals(SecurityConstants.HEAD_NURSE))) {
            throw new BadRequestException(ErrorKey.LICENSE_NUMBER);
        }

        Account director = accountRepository.findAccountByLogin(createdBy).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        createBaseAccount(medicalStaff, accessLevel, director);

        List<Specialization> specializationsList = specializations.stream()
                .map(name -> specializationRepository.findSpecializationByName(name).orElseThrow(() ->
                        new NotFoundException(ErrorKey.SPECIALIZATION_NOT_FOUND)))
                .collect(Collectors.toList());

        medicalStaff.setSpecializations(specializationsList);
        accountRepository.save(medicalStaff);

        urlService.createConfirmUrl(medicalStaff, director);
    }

    //endregion

    //region UPDATES

    public void changePassword(String login, Password oldPassword, Password newPassword) {
        Account account = accountRepository.findAccountByLogin(login).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        String hashedOldPassword = Sha512DigestUtils.shaHex(String.valueOf(oldPassword.getValue()));

        if (!hashedOldPassword.equals(account.getPassword())) {
            throw new BadRequestException(ErrorKey.PASSWORD_INCORRECT);
        }
        if (String.valueOf(newPassword.getValue()).equals(String.valueOf(oldPassword.getValue()))) {
            throw new ConflictException(ErrorKey.PASSWORD_THE_SAME);
        }

        account.setPassword(Sha512DigestUtils.shaHex(String.valueOf(newPassword.getValue())));
        account.setModificationDate(Timestamp.from(Instant.now()));
        accountRepository.save(account);

        emailSender.sendPasswordChangeEmail(account.getName(), account.getEmail());
    }

    public void changeActivity(String login, boolean newActivity, String modifiedBy) {
        Account account = accountRepository.findAccountByLogin(login).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        if (!account.isConfirmed()) {
            throw new ConflictException(ErrorKey.ACCOUNT_NOT_CONFIRMED);
        }

        account.setActive(newActivity);
        account.setModificationDate(Timestamp.from(Instant.now()));
        if (modifiedBy == null || login.equals(modifiedBy)) {
            account.setModifiedBy(null);
        } else {
            account.setModifiedBy(accountRepository.findAccountByLogin(modifiedBy).orElseThrow(() ->
                    new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND)));
        }

        accountRepository.save(account);

        emailSender.sendActivityChangedEmail(account.getName(), account.getEmail(), account.isActive());
    }

    private Account updateBaseAccount(Account account, String modifiedBy) {
        Account accountFromDB = accountRepository.findAccountByLogin(account.getLogin()).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

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
                    new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
            accountFromDB.setModifiedBy(accModifiedBy);
        }

        return accountFromDB;
    }

    public void updateAccountOffice(Account account, String modifiedBy) {

        accountRepository.save(updateBaseAccount(account, modifiedBy));

        emailSender.sendModificationEmail(account.getName(), account.getEmail());
    }

    public void updateMedicalStaff(MedicalStaff account, String modifiedBy, List<String> specializations) {

        MedicalStaff modifiedMedicalStaff = (MedicalStaff) updateBaseAccount(account, modifiedBy);

        if (account.getLicenseNr() != null && !account.getLicenseNr().isEmpty()) {
            modifiedMedicalStaff.setLicenseNr(account.getLicenseNr());
        }
        if (account.getAcademicDegree() != null && !account.getAcademicDegree().isEmpty()) {
            modifiedMedicalStaff.setAcademicDegree(account.getAcademicDegree());
        }
        if (specializations != null && !specializations.isEmpty()) {
            List<Specialization> specializationsList = specializations.stream()
                    .map(name -> specializationRepository.findSpecializationByName(name).orElseThrow(() ->
                            new NotFoundException(ErrorKey.SPECIALIZATION_NOT_FOUND)))
                    .collect(Collectors.toList());

            modifiedMedicalStaff.setSpecializations(specializationsList);
        } else {
            modifiedMedicalStaff.setSpecializations(Collections.emptyList());
        }

        accountRepository.save(modifiedMedicalStaff);

        emailSender.sendModificationEmail(account.getName(), account.getEmail());
    }

    public void confirmAccount(String urlCode, Password password) {
        Url url = urlService.validateUrl(urlCode, UrlActionType.CONFIRM.name());

        Account account = accountRepository.findAccountByLogin(url.getAccountEmployee().getLogin()).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        account.setPassword(Sha512DigestUtils.shaHex(String.valueOf(password.getValue())));
        account.setConfirmed(true);
        account.setModificationDate(Timestamp.from(Instant.now()));
        accountRepository.save(account);
        urlService.deleteUrl(url);

    }

    public void changeAccessLevel(String newAccessLevel, String login, String modifiedBy) {

        Account account = accountRepository.findAccountByLogin(login).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        AccessLevel accessLevel = accessLevelRepository.findAccessLevelByName(newAccessLevel).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCESS_LEVEL_NOT_FOUND));

        isAccessLevelConflict(accessLevel.getName(), account);

        account.setAccessLevel(accessLevel);
        account.setModificationDate(Timestamp.from(Instant.now()));

        if (modifiedBy.equals(account.getLogin())) {
            account.setModifiedBy(null);
        } else {
            Account accModifiedBy = accountRepository.findAccountByLogin(modifiedBy).orElseThrow(() ->
                    new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
            account.setModifiedBy(accModifiedBy);
        }

        accountRepository.save(account);

    }

    private void isAccessLevelConflict(String newAccessLevel, Account account) {
        if (account.getType().equals(AccountType.OFFICE.name())) {
            throw new ConflictException(ErrorKey.OFFICE_STAFF_ACCESS_LEVEL_CHANGE);
        }
        if (newAccessLevel.equals(AccessLevelName.SECRETARY)) {
            throw new ConflictException(ErrorKey.MEDICAL_STAFF_TO_OFFICE_CHANGE);
        }
        if (cannotChangeAccessLevel(newAccessLevel, account, AccessLevelName.TREATMENT_DIRECTOR)) {
            throw new ConflictException(ErrorKey.TREATMENT_DIRECTOR_REQUIRED);
        }
        if (cannotChangeAccessLevel(newAccessLevel, account, AccessLevelName.HEAD_NURSE)) {
            throw new ConflictException(ErrorKey.HEAD_NURSE_REQUIRED);
        }
    }

    private boolean cannotChangeAccessLevel(String newAccessLevel, Account account, String protectedAccessLevel) {
        return accountRepository.findAccountsByAccessLevel_Name(protectedAccessLevel).size() == 1 &&
                account.getAccessLevel().getName().equals(protectedAccessLevel) &&
                !newAccessLevel.equals(protectedAccessLevel);
    }

    public void changeEmailAddress(String newEmail, String login, String requestedBy) {

        if (!accountRepository.findAccountsByEmail(newEmail).isEmpty()) {
            throw new ConflictException(ErrorKey.EMAIL_UNIQUE);
        }

        Account account = accountRepository.findAccountByLogin(login).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        Account requestedByAccount = null;

        if (!login.equals(requestedBy)) {
            requestedByAccount = accountRepository.findAccountByLogin(requestedBy).orElseThrow(() ->
                    new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        }

        account.setEmail(newEmail);
        account.setModifiedBy(requestedByAccount);
        account.setModificationDate(Timestamp.from(Instant.now()));

        accountRepository.save(account);

        if (!account.isConfirmed() && requestedByAccount != null) {
            urlService.deleteOldConfirmationUrls(account);
            urlService.createConfirmUrl(account, requestedByAccount);
        }
    }

    public void resetPassword(String urlCode, Password newPassword) {
        Url url = urlService.validateUrl(urlCode, UrlActionType.PASSWORD.name());

        if (Sha512DigestUtils.shaHex(String.valueOf(newPassword.getValue())).equals(url.getAccountEmployee().getPassword())) {
            throw new ConflictException(ErrorKey.ERROR_SAME_PASSWORD);
        }

        Account account = url.getAccountEmployee();

        account.setPassword(Sha512DigestUtils.shaHex(String.valueOf(newPassword.getValue())));
        account.setModificationDate(Timestamp.from(Instant.now()));

        accountRepository.save(account);
        urlService.deleteUrl(url);
    }


    //endregion

    public void sendResetPasswordUrl(String email, String directorName, String directorSurname, String requestedBy) {
        Account accountEmployee = accountRepository.findAccountByEmailAndConfirmedIsTrue(email).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        Account accountDirector = accountRepository.findAccountByNameAndSurname(directorName, directorSurname)
                .orElseThrow(() -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        urlService.deleteOldAndCreateResetPasswordUrl(requestedBy.equals(accountDirector.getLogin()), accountEmployee,
                accountDirector, email);

    }

    public void deleteUnconfirmedAccount(String login) {
        Account account = accountRepository.findAccountByLogin(login).orElseThrow(
                () -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        if (account.isConfirmed()) {
            throw new ConflictException(ErrorKey.ACCOUNT_CONFIRMED);
        }

        urlService.deleteUrlsForAccount(account);
        accountRepository.delete(account);

        emailSender.sendRemovalEmail(account.getName(), account.getEmail());
    }

    public void sendConfirmationUrl(String login, String requestedBy) {
        Account accountEmployee = accountRepository.findAccountByLogin(login).orElseThrow(
                () -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        Account accountDirector = accountRepository.findAccountByLogin(requestedBy).orElseThrow(
                () -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));

        if (accountEmployee.isConfirmed()) {
            throw new ConflictException(ErrorKey.ACCOUNT_CONFIRMED);
        }

        urlService.deleteOldConfirmationUrls(accountEmployee);
        urlService.createConfirmUrl(accountEmployee, accountDirector);
    }

}
