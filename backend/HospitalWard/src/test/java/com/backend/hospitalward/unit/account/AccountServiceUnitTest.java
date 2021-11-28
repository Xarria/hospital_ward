package com.backend.hospitalward.unit.account;

import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.model.AccessLevel;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.model.common.AccessLevelName;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.service.AccountService;
import com.backend.hospitalward.service.UrlService;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.inject.Inject;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
class AccountServiceUnitTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    UrlService urlService;

    @Mock
    AccessLevelRepository accessLevelRepository;

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

        ArgumentCaptor<Account> accountCapture = ArgumentCaptor.forClass(Account.class);
        doNothing().when(accountRepository).save(accountCapture.capture());

        accountService.createAccount(accountToCreate, AccessLevelName.SECRETARY, "marek.woźniak1");

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
    }

    @Test
    void changePassword() {
    }

    @Test
    void changeActivity() {
    }

    @Test
    void updateAccountOffice() {
    }

    @Test
    void updateMedicalStaff() {
    }

    @Test
    void confirmAccount() {
    }

    @Test
    void changeAccessLevel() {
    }

    @Test
    void changeEmailAddress() {
    }

    @Test
    void resetPassword() {
    }

    @Test
    void sendResetPasswordUrl() {
    }

    @Test
    void deleteUnconfirmedAccount() {
    }

    @Test
    void sendConfirmationUrl() {
    }

    private void initAccounts() {
        accountActive = MedicalStaff.builder()
                .active(true)
                .name(AccountConstants.NEW_NAME)
                .surname(AccountConstants.NEW_SURNAME)
                .accessLevel(AccessLevel.builder().name(AccessLevelName.DOCTOR).build())
                .academicDegree(AccountConstants.NEW_DEGREE)
                .email(AccountConstants.NEW_EMAIL)
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
                .surname(AccountConstants.NEW_SURNAME)
                .accessLevel(AccessLevel.builder().name(AccessLevelName.SECRETARY).build())
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
                .name(AccountConstants.NEW_NAME)
                .surname(AccountConstants.NEW_SURNAME)
                .email(AccountConstants.NEW_EMAIL5)
                .licenseNr(AccountConstants.UPDATE_LICENSE_NR2)
                .academicDegree(AccountConstants.NEW_DEGREE)
                .build();

        accountList = List.of(accountActive, accountInactive, accountUnconfirmed);
    }
}
