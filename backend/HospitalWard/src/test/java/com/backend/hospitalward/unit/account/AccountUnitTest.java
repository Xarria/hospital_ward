package com.backend.hospitalward.unit.account;

import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.model.*;
import com.backend.hospitalward.model.common.AccessLevelName;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.SpecializationRepository;
import com.backend.hospitalward.service.AccountService;
import com.backend.hospitalward.service.UrlService;
import com.backend.hospitalward.util.notification.EmailSender;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.security.enterprise.credential.Password;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
class AccountUnitTest {

    public static final int SECONDS_THRESHOLD = 2;
    @Mock
    AccountRepository accountRepository;

    @Mock
    UrlService urlService;

    @Mock
    AccessLevelRepository accessLevelRepository;

    @Mock
    SpecializationRepository specializationRepository;

    @Mock
    EmailSender emailSender;

    @InjectMocks
    AccountService accountService;

    List<Account> accountList;
    MedicalStaff accountActive;
    Account accountInactive;
    Account accountUnconfirmed;
    Account accountToCreate;
    MedicalStaff medicalStaffToCreate;

    @BeforeEach
    void setUp() {
        initAccounts();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllAccounts() {
        when(accountRepository.findAll()).thenReturn(accountList);

        List<Account> allAccounts = accountService.getAllAccounts();

        assertAll(
                () -> assertEquals(accountList.size(), allAccounts.size())
        );
    }

    @Test
    void getAccountByLogin() {
        when(accountRepository.findAccountByLogin("marek.woźniak1")).thenReturn(Optional.of(accountActive));

        Account byLogin = accountService.getAccountByLogin("marek.woźniak1");

        assertAll(
                () -> assertNotNull(byLogin)
        );

    }

    @Test
    void createAccount() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountsByLoginContains("marek.woźniak"))
                .thenReturn(List.of(accountActive, accountInactive, accountUnconfirmed));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.SECRETARY).build()));
        doNothing().when(urlService).createConfirmUrl(any(), any());

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);

        accountService.createAccount(accountToCreate, AccessLevelName.SECRETARY, "marek.woźniak1");

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertEquals("marek.woźniak4", accountCapture.getValue().getLogin()),
                () -> assertEquals(0L, accountCapture.getValue().getVersion()),
                () -> assertEquals(accountActive, accountCapture.getValue().getCreatedBy()),
                () -> assertEquals(AccessLevelName.SECRETARY, accountCapture.getValue().getAccessLevel().getName())
        );
    }

    @Test
    void createMedicalStaff() {
        when(accountRepository.findAccountByLogin("marek.woźniak1"))
                .thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountsByLoginContains("marek.woźniak"))
                .thenReturn(List.of(accountActive, accountInactive, accountUnconfirmed));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.DOCTOR).build()));
        when(specializationRepository.findSpecializationByName(any())).thenReturn(Optional.of(new Specialization()));
        doNothing().when(urlService).createConfirmUrl(any(), any());

        ArgumentCaptor<MedicalStaff> accountCapture = ArgumentCaptor.forClass(MedicalStaff.class);

        accountService.createMedicalStaff(medicalStaffToCreate, AccessLevelName.DOCTOR, List.of("Kardiologia"),
                "marek.woźniak1");

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertEquals("marek.woźniak4", accountCapture.getValue().getLogin()),
                () -> assertEquals(0L, accountCapture.getValue().getVersion()),
                () -> assertEquals(accountActive, accountCapture.getValue().getCreatedBy()),
                () -> assertEquals(AccessLevelName.DOCTOR, accountCapture.getValue().getAccessLevel().getName()),
                () -> assertNotNull(accountCapture.getValue().getSpecializations())
        );
    }

    @Test
    void changePassword() {
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.of(accountActive));
        doNothing().when(emailSender).sendPasswordChangeEmail(any(), any());

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);

        accountService.changePassword("marek.woźniak", new Password("password"),
                new Password("newPassword"));

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertTrue(accountCapture.getValue().getModificationDate().after(Timestamp.from(Instant.now()
                        .minusSeconds(SECONDS_THRESHOLD))))
        );
    }

    @Test
    void changeActivity() {
        when(accountRepository.findAccountByLogin("marek.woźniak2"))
                .thenReturn(Optional.of(accountInactive));
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.of(accountActive));

        doNothing().when(emailSender).sendPasswordChangeEmail(any(), any());

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);

        accountService.changeActivity("marek.woźniak2", true, "marek.woźniak");

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertTrue(accountCapture.getValue().getModificationDate().after(Timestamp.from(Instant.now()
                        .minusSeconds(SECONDS_THRESHOLD)))),
                () -> assertEquals(accountActive, accountCapture.getValue().getModifiedBy()),
                () -> assertTrue(accountCapture.getValue().isActive())
        );
    }

    @Test
    void updateAccountOffice() {
        when(accountRepository.findAccountByLogin(null))
                .thenReturn(Optional.of(accountToCreate));
        when(accountRepository.findAccountByLogin("marek.woźniak"))
                .thenReturn(Optional.of(accountActive));
        doNothing().when(emailSender).sendModificationEmail(any(), any());

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);

        accountService.updateAccountOffice(accountToCreate, accountActive.getLogin());

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertTrue(accountCapture.getValue().getModificationDate().after(Timestamp.from(Instant.now()
                        .minusSeconds(SECONDS_THRESHOLD)))),
                () -> assertEquals(accountActive, accountCapture.getValue().getModifiedBy()),
                () -> assertEquals(accountToCreate.getName(), accountCapture.getValue().getName()),
                () -> assertEquals(accountToCreate.getSurname(), accountCapture.getValue().getSurname()),
                () -> assertEquals(accountToCreate.getEmail(), accountCapture.getValue().getEmail())
        );
    }

    @Test
    void updateMedicalStaff() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountActive));
        when(specializationRepository.findSpecializationByName(any())).thenReturn(Optional.of(new Specialization()));
        doNothing().when(emailSender).sendModificationEmail(any(), any());

        ArgumentCaptor<MedicalStaff> accountCapture = ArgumentCaptor.forClass(MedicalStaff.class);

        accountService.updateMedicalStaff(medicalStaffToCreate, accountActive.getLogin(), List.of("Spec1", "Spec2"));

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertTrue(accountCapture.getValue().getModificationDate().after(Timestamp.from(Instant.now()
                        .minusSeconds(SECONDS_THRESHOLD)))),
                () -> assertNull(accountCapture.getValue().getModifiedBy()),
                () -> assertEquals(medicalStaffToCreate.getName(), accountCapture.getValue().getName()),
                () -> assertEquals(medicalStaffToCreate.getSurname(), accountCapture.getValue().getSurname()),
                () -> assertEquals(medicalStaffToCreate.getEmail(), accountCapture.getValue().getEmail()),
                () -> assertEquals(medicalStaffToCreate.getLicenseNr(), accountCapture.getValue().getLicenseNr()),
                () -> assertEquals(2, accountCapture.getValue().getSpecializations().size())
        );
    }

    @Test
    void confirmAccount() {
        when(urlService.validateUrl(any(), any())).thenReturn(Url.builder().accountEmployee(accountUnconfirmed).build());
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountUnconfirmed));
        doNothing().when(urlService).deleteUrl(any());

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);

        accountService.confirmAccount("1234567890", new Password(accountActive.getPassword()));

        verify(urlService).deleteUrl(any());
        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertTrue(accountCapture.getValue().getModificationDate().after(Timestamp.from(Instant.now()
                        .minusSeconds(SECONDS_THRESHOLD)))),
                () -> assertTrue(accountCapture.getValue().isConfirmed())
        );
    }

    @Test
    void changeAccessLevel() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountActive));
        when(accessLevelRepository.findAccessLevelByName(any()))
                .thenReturn(Optional.of(AccessLevel.builder().name("HEAD NURSE").build()));

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);

        accountService.changeAccessLevel("HEAD NURSE", "marek.woźniak", "marek.woźniak");

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertTrue(accountCapture.getValue().getModificationDate().after(Timestamp.from(Instant.now()
                        .minusSeconds(SECONDS_THRESHOLD)))),
                () -> assertEquals(AccessLevelName.HEAD_NURSE, accountCapture.getValue().getAccessLevel().getName()),
                () -> assertNull(accountCapture.getValue().getModifiedBy())
        );
    }

    @Test
    void changeEmailAddress() {
        when(accountRepository.findAccountsByEmail(any())).thenReturn(Collections.emptyList());
        when(accountRepository.findAccountByLogin("login")).thenReturn(Optional.of(accountInactive));
        when(accountRepository.findAccountByLogin("marek.woźniak")).thenReturn(Optional.of(accountActive));

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);

        accountService.changeEmailAddress("newEmail@mail.com", "login", "marek.woźniak");

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertTrue(accountCapture.getValue().getModificationDate().after(Timestamp.from(Instant.now()
                        .minusSeconds(SECONDS_THRESHOLD)))),
                () -> assertEquals("newEmail@mail.com", accountCapture.getValue().getEmail()),
                () -> assertEquals(accountActive, accountCapture.getValue().getModifiedBy())
        );
    }

    @Test
    void resetPassword() {
        when(urlService.validateUrl(any(), any())).thenReturn(Url.builder().accountEmployee(accountActive).build());
        doNothing().when(urlService).deleteUrl(any());

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);

        accountService.resetPassword("1234567890", new Password("resetPassword"));

        verify(accountRepository).save(accountCapture.capture());

        assertAll(
                () -> assertNotNull(accountCapture.getValue()),
                () -> assertTrue(accountCapture.getValue().getModificationDate().after(Timestamp.from(Instant.now()
                        .minusSeconds(SECONDS_THRESHOLD))))
        );
    }

    @Test
    void sendResetPasswordUrl() {
        when(accountRepository.findAccountByEmailAndConfirmedIsTrue(any())).thenReturn(Optional.of(accountInactive));
        when(accountRepository.findAccountByNameAndSurname(any(), any())).thenReturn(Optional.of(accountActive));
        doNothing().when(urlService).deleteOldAndCreateResetPasswordUrl(anyBoolean(), any(), any(), any());

        accountService.sendResetPasswordUrl("mail@wp.pl", "Marek",
                "Woźniak", "marek.woźniak");

        verify(urlService).deleteOldAndCreateResetPasswordUrl(anyBoolean(), any(), any(), any());
    }

    @Test
    void deleteUnconfirmedAccount() {
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(accountUnconfirmed));
        doNothing().when(urlService).deleteUrlsForAccount(any());
        doNothing().when(accountRepository).delete(any());
        doNothing().when(emailSender).sendRemovalEmail(any(), any());

        accountService.deleteUnconfirmedAccount("login");

        verify(accountRepository).delete(accountUnconfirmed);
        verify(emailSender).sendRemovalEmail(any(), any());
    }

    @Test
    void sendConfirmationUrl() {
        when(accountRepository.findAccountByLogin("marek.woźniak")).thenReturn(Optional.of(accountActive));
        when(accountRepository.findAccountByLogin("marek.woźniak3")).thenReturn(Optional.of(accountUnconfirmed));
        doNothing().when(urlService).deleteOldConfirmationUrls(any());
        doNothing().when(urlService).createConfirmUrl(any(), any());

        accountService.sendConfirmationUrl("marek.woźniak3", "marek.woźniak");

        verify(urlService).deleteOldConfirmationUrls(any());
        verify(urlService).createConfirmUrl(any(), any());


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

        accountList = List.of(accountActive, accountInactive, accountUnconfirmed);
    }
}
