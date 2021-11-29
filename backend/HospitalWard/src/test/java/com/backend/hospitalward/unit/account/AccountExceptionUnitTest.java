package com.backend.hospitalward.unit.account;

import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.AccessLevel;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.model.Url;
import com.backend.hospitalward.model.common.AccessLevelName;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.SpecializationRepository;
import com.backend.hospitalward.service.AccountService;
import com.backend.hospitalward.service.UrlService;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.security.enterprise.credential.Password;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AccountExceptionUnitTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    UrlService urlService;

    @Mock
    AccessLevelRepository accessLevelRepository;

    @Mock
    SpecializationRepository specializationRepository;

    @InjectMocks
    AccountService accountService;

    MedicalStaff accountActive;
    Account accountInactive;
    Account accountUnconfirmed;
    Account accountToCreate;
    MedicalStaff accountDirector;
    MedicalStaff medicalStaffToCreate;
    MedicalStaff accountHeadNurse;

    @BeforeEach
    void setUp() {
        initAccounts();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowExceptionWhenGetAccountByLoginNotFound() {
        when(accountRepository.findAccountByLogin("marek.woźniak1")).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, () -> accountService.getAccountByLogin("marek.woźniak1"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateAccountAccountDirectorNotFound() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.empty());
        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.createAccount(accountToCreate, AccessLevelName.SECRETARY, "marek.woźniak1"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateAccountOfficeWithMedicAccessLevel() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountsByLoginContains("marek.woźniak"))
                .thenReturn(List.of(accountActive, accountInactive, accountUnconfirmed));

        Exception e = assertThrows(BadRequestException.class, ()
                -> accountService.createAccount(accountToCreate, AccessLevelName.DOCTOR, "marek.woźniak1"));

        assertEquals(ErrorKey.ACCESS_LEVEL_INVALID_OFFICE, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateAccountAccessLevelNotFound() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountsByLoginContains("marek.woźniak"))
                .thenReturn(List.of(accountActive, accountInactive, accountUnconfirmed));
        when(accessLevelRepository.findAccessLevelByName(any())).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.createAccount(accountToCreate, AccessLevelName.SECRETARY, "marek.woźniak1"));

        assertEquals(ErrorKey.ACCESS_LEVEL_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateMedicalStaffWithOfficeAccessLevel() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountsByLoginContains("marek.woźniak"))
                .thenReturn(List.of(accountActive, accountInactive, accountUnconfirmed));

        Exception e = assertThrows(BadRequestException.class, ()
                -> accountService.createMedicalStaff(medicalStaffToCreate, AccessLevelName.SECRETARY,
                List.of("Kardiologia"), "marek.woźniak1"));

        assertEquals(ErrorKey.ACCESS_LEVEL_INVALID_MEDIC, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateHeadNurseWithInvalidLicenseNo() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountsByLoginContains("marek.woźniak"))
                .thenReturn(List.of(accountActive, accountInactive, accountUnconfirmed));

        Exception e = assertThrows(BadRequestException.class, ()
                -> accountService.createMedicalStaff(medicalStaffToCreate, AccessLevelName.HEAD_NURSE,
                List.of("Kardiologia"), "marek.woźniak1"));

        assertEquals(ErrorKey.LICENSE_NUMBER, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateDoctorWithInvalidLicenseNo() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountsByLoginContains("marek.woźniak"))
                .thenReturn(List.of(accountActive, accountInactive, accountUnconfirmed));

        medicalStaffToCreate.setLicenseNr("123456P");

        Exception e = assertThrows(BadRequestException.class, ()
                -> accountService.createMedicalStaff(medicalStaffToCreate, AccessLevelName.DOCTOR,
                List.of("Kardiologia"), "marek.woźniak1"));

        assertEquals(ErrorKey.LICENSE_NUMBER, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateMedicalStaffWhenSpecializationNotFound() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountsByLoginContains("marek.woźniak"))
                .thenReturn(List.of(accountActive, accountInactive, accountUnconfirmed));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.DOCTOR).build()));
        when(specializationRepository.findSpecializationByName(any())).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.createMedicalStaff(medicalStaffToCreate, AccessLevelName.DOCTOR,
                List.of("Kardiologia"), "marek.woźniak1"));

        assertEquals(ErrorKey.SPECIALIZATION_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangePasswordAccountNotFound() {
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.empty());
        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.changePassword("marek.woźniak", new Password("password"),
                new Password("newPassword")));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangePasswordNewPasswordSameAsOld() {
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.of(accountActive));
        Exception e = assertThrows(ConflictException.class, ()
                -> accountService.changePassword("marek.woźniak", new Password("password"),
                new Password("password")));

        assertEquals(ErrorKey.PASSWORD_THE_SAME, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangePasswordOldPasswordIncorrect() {
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.of(accountActive));
        Exception e = assertThrows(BadRequestException.class, ()
                -> accountService.changePassword("marek.woźniak", new Password("incorrect"),
                new Password("newPassword")));

        assertEquals(ErrorKey.PASSWORD_INCORRECT, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeActivityAccpuntNotFound() {
        when(accountRepository.findAccountByLogin("marek.woźniak2"))
                .thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.changeActivity("marek.woźniak2", true, "marek.woźniak"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeActivityAccountUnconfirmed() {
        when(accountRepository.findAccountByLogin("marek.woźniak2"))
                .thenReturn(Optional.of(accountUnconfirmed));
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.of(accountActive));

        Exception e = assertThrows(ConflictException.class, ()
                -> accountService.changeActivity("marek.woźniak2", true, "marek.woźniak"));

        assertEquals(ErrorKey.ACCOUNT_NOT_CONFIRMED, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeActivityAccountDrectorNotFound() {
        when(accountRepository.findAccountByLogin("marek.woźniak2"))
                .thenReturn(Optional.of(accountInactive));
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.changeActivity("marek.woźniak2", true, "marek.woźniak"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateAccountOfficeAccountDirectorNotFound() {
        when(accountRepository.findAccountByLogin(null))
                .thenReturn(Optional.of(accountToCreate));
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.updateAccountOffice(accountToCreate, accountActive.getLogin()));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateAccountOfficeAccountNotFound() {
        when(accountRepository.findAccountByLogin(any()))
                .thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.updateAccountOffice(accountToCreate, accountActive.getLogin()));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateMedicalStaffSpecializationNotFound() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountActive));
        when(specializationRepository.findSpecializationByName(any())).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, () ->
                accountService.updateMedicalStaff(medicalStaffToCreate, accountActive.getLogin(), List.of("Spec1")));

        assertEquals(ErrorKey.SPECIALIZATION_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenConfirmAccountAccountNotFound() {
        when(urlService.validateUrl(any(), any())).thenReturn(Url.builder().accountEmployee(accountUnconfirmed).build());
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, () ->
                accountService.confirmAccount("1234567890", new Password("password")));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeAccessLevelAccountDirectorNotFound() {
        when(accountRepository.findAccountByLogin("login")).thenReturn(Optional.of(accountActive));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.DOCTOR).build()));
        when(accountRepository.findAccountByLogin("login2")).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, () ->
                accountService.changeAccessLevel("DOCTOR", "login", "login2"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeAccessLevelNoHeadNurseLeft() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountHeadNurse));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.DOCTOR).build()));
        when(accountRepository.findAccountsByAccessLevel_Name(AccessLevelName.HEAD_NURSE))
                .thenReturn(List.of(accountHeadNurse));

        Exception e = assertThrows(ConflictException.class, () ->
                accountService.changeAccessLevel("DOCTOR", "login", "login"));

        assertEquals(ErrorKey.HEAD_NURSE_REQUIRED, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeAccessLevelNoTreatmentDirectorLeft() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountDirector));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.HEAD_NURSE).build()));
        when(accountRepository.findAccountsByAccessLevel_Name(AccessLevelName.TREATMENT_DIRECTOR))
                .thenReturn(List.of(accountDirector));

        Exception e = assertThrows(ConflictException.class, () ->
                accountService.changeAccessLevel(AccessLevelName.HEAD_NURSE, "login", "login"));

        assertEquals(ErrorKey.TREATMENT_DIRECTOR_REQUIRED, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeAccessLevelMedicToSecretary() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountActive));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.SECRETARY).build()));

        Exception e = assertThrows(ConflictException.class, () ->
                accountService.changeAccessLevel("SECRETARY", "login", "login"));

        assertEquals(ErrorKey.MEDICAL_STAFF_TO_OFFICE_CHANGE, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeAccessLevelAccountTypeOffice() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountInactive));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.HEAD_NURSE).build()));

        Exception e = assertThrows(ConflictException.class, () ->
                accountService.changeAccessLevel("HEAD NURSE", "login", "login"));

        assertEquals(ErrorKey.OFFICE_STAFF_ACCESS_LEVEL_CHANGE, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeAccessLevelAccountNotFound() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, () ->
                accountService.changeAccessLevel("HEAD NURSE", "login", "login"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeEmailAddressEmailNotUnique() {
        when(accountRepository.findAccountsByEmail(any())).thenReturn(List.of(accountActive));

        Exception e = assertThrows(ConflictException.class, ()
                -> accountService.changeEmailAddress("newEmail@mail.com", "login", "login"));

        assertEquals(ErrorKey.EMAIL_UNIQUE, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChangeEmailAddressAccountNotFound() {
        when(accountRepository.findAccountsByEmail(any())).thenReturn(Collections.emptyList());
        when(accountRepository.findAccountByLogin("login")).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.changeEmailAddress("newEmail@mail.com", "login", "login"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptonWhenChangeEmailAddressAccountDirectorNotFound() {
        when(accountRepository.findAccountsByEmail(any())).thenReturn(Collections.emptyList());
        when(accountRepository.findAccountByLogin("login")).thenReturn(Optional.of(accountInactive));
        when(accountRepository.findAccountByLogin("login2")).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.changeEmailAddress("newEmail@mail.com", "login", "login2"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenResetPasswordOldPasswordTheSameAsNew() {
        when(urlService.validateUrl(any(), any())).thenReturn(Url.builder().accountEmployee(accountActive).build());

        Exception e = assertThrows(ConflictException.class, ()
                -> accountService.resetPassword("1234567890", new Password("password")));

        assertEquals(ErrorKey.ERROR_SAME_PASSWORD, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSendResetPasswordUrlAccountDirectorNotFound() {
        when(accountRepository.findAccountByEmailAndConfirmedIsTrue(any())).thenReturn(Optional.of(accountInactive));
        when(accountRepository.findAccountByNameAndSurname(any(), any())).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.sendResetPasswordUrl("newEmail@mail.com", "Name",
                "Surname", "login2"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSendResetPasswordUrlAccounNotFound() {
        when(accountRepository.findAccountByEmailAndConfirmedIsTrue(any())).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.sendResetPasswordUrl("newEmail@mail.com", "Name",
                "Surname", "login2"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeleteUnconfirmedAccountAccountNotFound() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.deleteUnconfirmedAccount("login"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeleteUnconfirmedAccountAccountConfirmed() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountActive));

        Exception e = assertThrows(ConflictException.class, ()
                -> accountService.deleteUnconfirmedAccount("login"));

        assertEquals(ErrorKey.ACCOUNT_CONFIRMED, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSendConfirmationUrlAccountNotFound() {
        when(accountRepository.findAccountByLogin("login")).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.sendConfirmationUrl("login", "login2"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSendConfirmationUrlAccountDirectorNotFound() {
        when(accountRepository.findAccountByLogin("login")).thenReturn(Optional.of(accountUnconfirmed));
        when(accountRepository.findAccountByLogin("login2")).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, ()
                -> accountService.sendConfirmationUrl("login", "login2"));

        assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSendConfirmationUrlAccountConfirmed() {
        when(accountRepository.findAccountByLogin("login")).thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountByLogin("login2")).thenReturn(Optional.of(accountDirector));

        Exception e = assertThrows(ConflictException.class, ()
                -> accountService.sendConfirmationUrl("login", "login2"));

        assertEquals(ErrorKey.ACCOUNT_CONFIRMED, e.getMessage());
    }

    private void initAccounts() {
        accountActive = MedicalStaff.builder()
                .active(true)
                .name(AccountConstants.NEW_NAME)
                .password("b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df" +
                        "5f1326af5a2ea6d103fd07c95385ffab0cacbc86")
                .surname(AccountConstants.NEW_SURNAME)
                .accessLevel(AccessLevel.builder().name(AccessLevelName.DOCTOR).build())
                .academicDegree(AccountConstants.NEW_DEGREE)
                .email(AccountConstants.NEW_EMAIL)
                .type("MEDIC")
                .confirmed(true)
                .version(0L)
                .licenseNr(AccountConstants.NEW_LICENSE_NR)
                .login("marek.woźniak")
                .creationDate(Timestamp.from(Instant.now()))
                .build();

        accountDirector = MedicalStaff.builder()
                .active(true)
                .name(AccountConstants.NEW_NAME)
                .password("b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df" +
                        "5f1326af5a2ea6d103fd07c95385ffab0cacbc86")
                .surname(AccountConstants.NEW_SURNAME)
                .accessLevel(AccessLevel.builder().name(AccessLevelName.TREATMENT_DIRECTOR).build())
                .academicDegree(AccountConstants.NEW_DEGREE)
                .email(AccountConstants.NEW_EMAIL5)
                .type("MEDIC")
                .confirmed(true)
                .version(0L)
                .licenseNr(AccountConstants.UPDATE_LICENSE_NR2)
                .login("marek.woźniak7")
                .creationDate(Timestamp.from(Instant.now()))
                .build();

        accountHeadNurse = MedicalStaff.builder()
                .active(true)
                .name(AccountConstants.NEW_NAME)
                .password("b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df" +
                        "5f1326af5a2ea6d103fd07c95385ffab0cacbc86")
                .surname(AccountConstants.NEW_SURNAME)
                .accessLevel(AccessLevel.builder().name(AccessLevelName.HEAD_NURSE).build())
                .academicDegree(AccountConstants.NEW_DEGREE)
                .email(AccountConstants.NEW_EMAIL5)
                .type("MEDIC")
                .confirmed(true)
                .version(0L)
                .licenseNr(AccountConstants.UPDATE_LICENSE_NR)
                .login("marek.woźniak6")
                .creationDate(Timestamp.from(Instant.now()))
                .build();

        accountInactive = Account.builder()
                .active(false)
                .version(0L)
                .name(AccountConstants.NEW_NAME)
                .password("b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df" +
                        "5f1326af5a2ea6d103fd07c95385ffab0cacbc86")
                .surname(AccountConstants.NEW_SURNAME)
                .accessLevel(AccessLevel.builder().name(AccessLevelName.SECRETARY).build())
                .type("OFFICE")
                .email(AccountConstants.NEW_EMAIL2)
                .confirmed(true)
                .login("marek.woźniak2")
                .creationDate(Timestamp.from(Instant.now()))
                .build();

        accountUnconfirmed = Account.builder()
                .active(false)
                .version(0L)
                .name(AccountConstants.NEW_NAME)
                .surname(AccountConstants.NEW_SURNAME)
                .accessLevel(AccessLevel.builder().name(AccessLevelName.SECRETARY).build())
                .email(AccountConstants.NEW_EMAIL3)
                .confirmed(false)
                .login("marek.woźniak3")
                .creationDate(Timestamp.from(Instant.now()))
                .build();

        accountToCreate = Account.builder()
                .name(AccountConstants.NEW_NAME)
                .surname(AccountConstants.NEW_SURNAME)
                .email(AccountConstants.NEW_EMAIL4)
                .build();

        medicalStaffToCreate = MedicalStaff.builder()
                .login("marek.woźniak")
                .name(AccountConstants.NEW_NAME)
                .surname(AccountConstants.NEW_SURNAME)
                .email(AccountConstants.NEW_EMAIL5)
                .licenseNr(AccountConstants.UPDATE_LICENSE_NR2)
                .academicDegree(AccountConstants.NEW_DEGREE)
                .build();

    }
}
