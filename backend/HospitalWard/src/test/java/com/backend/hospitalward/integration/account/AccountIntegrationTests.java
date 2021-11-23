package com.backend.hospitalward.integration.account;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import com.backend.hospitalward.dto.request.account.ChangePasswordRequest;
import com.backend.hospitalward.dto.request.account.ResetPasswordRequest;
import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffUpdateRequest;
import com.backend.hospitalward.dto.response.account.AccountGeneralResponse;
import com.backend.hospitalward.dto.response.medicalStaff.MedicalStaffGeneralResponse;
import com.backend.hospitalward.integration.AbstractTestContainer;
import com.backend.hospitalward.integration.common.AccountConstants;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.model.Url;
import com.backend.hospitalward.repository.UrlRepository;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.AccountService;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import liquibase.pro.packaged.T;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountIntegrationTests extends AbstractTestContainer {

    @Autowired
    AccountService accountService;

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    TestRestTemplate restTemplate;

    Gson gson;

    String token;

    String valid_password = AccountConstants.SG_PASSWORD;

    @BeforeEach
    public void authenticate() {
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(AccountConstants.SG_LOGIN, valid_password), null);
        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH), HttpMethod.POST, credentials, String.class);

        token = response.getBody();
    }

    @BeforeAll
    public void setUpGson() {
        GsonFireBuilder builder = new GsonFireBuilder()
                .registerTypeSelector(AccountGeneralResponse.class, readElement -> {
                    String type = readElement.getAsJsonObject().get(AccountConstants.GSON_TYPE_IDENTIFIER).getAsString();
                    if (type.equals(AccountConstants.TYPE_MEDIC)) {
                        return MedicalStaffGeneralResponse.class;
                    } else if (type.equals(AccountConstants.TYPE_OFFICE)) {
                        return AccountGeneralResponse.class;
                    } else {
                        return null;
                    }
                });
        gson = builder.createGson();
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER + token);
        return headers;
    }

    @NotNull
    private HttpEntity<T> getJwtHttpEntity() {

        return new HttpEntity<>(null, getHttpHeaders());
    }

    @NotNull
    private Timestamp getTimestampToCompare() {
        return Timestamp.from(Instant.now().minusSeconds(5));
    }

    @Order(1)
    @Test
    void getAllAccounts() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS),
                HttpMethod.GET, jwtToken, String.class);

        List<AccountGeneralResponse> accounts = Arrays.asList(gson.fromJson(response.getBody(), AccountGeneralResponse[].class));

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(accountService.getAllAccounts().size(), accounts.size()),
                () -> assertNotNull(accounts.get(0)),
                () -> assertEquals(AccountConstants.SG_LOGIN, accounts.get(0).getLogin())
        );

    }

    @Order(2)
    @Test
    void getAccountByLogin() {

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS +
                "/" + AccountConstants.SG_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        AccountGeneralResponse account = gson.fromJson(response.getBody(), AccountGeneralResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getHeaders().get(HttpHeaders.ETAG)),
                () -> assertNotNull(account),
                () -> assertEquals(AccountConstants.SG_LOGIN, account.getLogin()),
                () -> assertEquals(AccountConstants.SG_LICENSE_NR, ((MedicalStaffGeneralResponse) account).getLicenseNr())
        );
    }

    @Order(3)
    @Test
    void getProfile() {

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_PROFILE),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        AccountGeneralResponse account = gson.fromJson(response.getBody(), AccountGeneralResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getHeaders().get(HttpHeaders.ETAG)),
                () -> assertNotNull(account),
                () -> assertEquals(AccountConstants.SG_LOGIN, account.getLogin()),
                () -> assertEquals(AccountConstants.SG_LICENSE_NR, ((MedicalStaffGeneralResponse) account).getLicenseNr())
        );
    }

    @Order(4)
    @Test
    void createAccountOffice() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<AccountCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountCreateRequest.builder()
                        .name(AccountConstants.NEW_NAME)
                        .accessLevel(AccountConstants.NEW_ACCESS_LEVEL)
                        .email(AccountConstants.NEW_EMAIL)
                        .surname(AccountConstants.NEW_SURNAME)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_OFFICE),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(accountService.getAccountByLogin(AccountConstants.NEW_LOGIN1)),
                () -> assertTrue(urlRepository.findAll().get((int) urlRepository.count() - 1)
                        .getAccountEmployee().getLogin().contains(AccountConstants.NEW_LOGIN1)),
                () -> assertEquals(AccountConstants.NEW_ACCESS_LEVEL, accountService.getAccountByLogin(
                        AccountConstants.NEW_LOGIN1).getAccessLevel().getName()),
                () -> assertEquals(AccountConstants.TYPE_OFFICE, accountService.getAccountByLogin(
                        AccountConstants.NEW_LOGIN1).getType()),
                () -> assertEquals(AccountConstants.SG_LOGIN, accountService.getAccountByLogin(AccountConstants.NEW_LOGIN1)
                        .getCreatedBy().getLogin())
        );
    }

    @Order(5)
    @Test
    void createAccountMedic() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<MedicalStaffCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffCreateRequest.builder()
                        .name(AccountConstants.NEW_NAME2)
                        .accessLevel(AccountConstants.NEW_ACCESS_LEVEL2)
                        .email(AccountConstants.NEW_EMAIL2)
                        .surname(AccountConstants.NEW_SURNAME)
                        .licenseNr(AccountConstants.NEW_LICENSE_NR)
                        .academicDegree(AccountConstants.NEW_DEGREE)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_MEDIC),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(accountService.getAccountByLogin(AccountConstants.NEW_LOGIN2)),
                () -> assertTrue(urlRepository.findAll().get((int) urlRepository.count() - 1)
                        .getAccountEmployee().getLogin().contains(AccountConstants.NEW_LOGIN2)),
                () -> assertEquals(AccountConstants.NEW_ACCESS_LEVEL2, accountService.getAccountByLogin(
                        AccountConstants.NEW_LOGIN2).getAccessLevel().getName()),
                () -> assertEquals(AccountConstants.TYPE_MEDIC, accountService.getAccountByLogin(
                        AccountConstants.NEW_LOGIN2).getType()),
                () -> assertEquals(AccountConstants.SG_LOGIN, accountService.getAccountByLogin(AccountConstants.NEW_LOGIN2)
                        .getCreatedBy().getLogin())
        );
    }

    @Order(6)
    @Test
    void changePassword() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<ChangePasswordRequest> changePasswordRequestHttpEntity = new HttpEntity<>(
                ChangePasswordRequest.builder()
                        .oldPassword(AccountConstants.SG_PASSWORD)
                        .newPassword(AccountConstants.NEW_SG_PASSWORD)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CHANGE_PASSWORD),
                HttpMethod.PUT, changePasswordRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.SG_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );

        valid_password = AccountConstants.NEW_SG_PASSWORD;
    }


    @Order(7)
    @Test
    void deactivateAccount() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.DEACTIVATE
                + AccountConstants.JK_LOGIN), HttpMethod.PUT, jwtToken, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertFalse(accountService.getAccountByLogin(AccountConstants.JK_LOGIN).isActive()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.JK_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(8)
    @Test
    void activateAccount() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.ACTIVATE
                + AccountConstants.JK_LOGIN), HttpMethod.PUT, jwtToken, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.JK_LOGIN).isActive()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.JK_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(9)
    @Test
    void updateAccountOffice() {

        Long version = accountService.getAccountByLogin(
                AccountConstants.OFFICE_LOGIN).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS +
                "/" + AccountConstants.OFFICE_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<AccountUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountUpdateRequest.builder()
                        .login(AccountConstants.OFFICE_LOGIN)
                        .name(AccountConstants.UPDATE_NAME)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.UPDATE_OFFICE
                + AccountConstants.OFFICE_LOGIN), HttpMethod.PUT, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(AccountConstants.UPDATE_NAME, accountService.getAccountByLogin(
                        AccountConstants.OFFICE_LOGIN).getName()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.OFFICE_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(10)
    @Test
    void updateAccountMedic() {

        Long version = accountService.getAccountByLogin(
                AccountConstants.SG_LOGIN).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS +
                "/" + AccountConstants.SG_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<MedicalStaffUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffUpdateRequest.builder()
                        .login(AccountConstants.SG_LOGIN)
                        .licenseNr(AccountConstants.UPDATE_LICENSE_NR)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.UPDATE_MEDIC
                + AccountConstants.SG_LOGIN), HttpMethod.PUT, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(AccountConstants.UPDATE_LICENSE_NR, ((MedicalStaff) accountService.getAccountByLogin(
                        AccountConstants.SG_LOGIN)).getLicenseNr()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.SG_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(11)
    @Test
    void updateOwnAccountOffice() {
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(AccountConstants.OFFICE_LOGIN, AccountConstants.SG_PASSWORD), null);
        ResponseEntity<String> responseAuth = restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH),
                HttpMethod.POST, credentials, String.class);

        token = responseAuth.getBody();

        Long version = accountService.getAccountByLogin(
                AccountConstants.OFFICE_LOGIN).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_PROFILE),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<AccountUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountUpdateRequest.builder()
                        .login(AccountConstants.OFFICE_LOGIN)
                        .surname(AccountConstants.UPDATE_NAME)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.UPDATE_OWN_OFFICE)
                , HttpMethod.PUT, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(AccountConstants.UPDATE_NAME, accountService.getAccountByLogin(
                        AccountConstants.OFFICE_LOGIN).getSurname()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.OFFICE_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(12)
    @Test
    void updateOwnAccountMedic() {

        Long version = accountService.getAccountByLogin(
                AccountConstants.SG_LOGIN).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS +
                "/" + AccountConstants.SG_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<MedicalStaffUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffUpdateRequest.builder()
                        .login(AccountConstants.SG_LOGIN)
                        .licenseNr(AccountConstants.UPDATE_LICENSE_NR2)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.UPDATE_OWN_MEDIC)
                , HttpMethod.PUT, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(AccountConstants.UPDATE_LICENSE_NR2, ((MedicalStaff) accountService.getAccountByLogin(
                        AccountConstants.SG_LOGIN)).getLicenseNr()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.SG_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(13)
    @Test
    void changeAccessLevel() {

        HttpEntity<String> changeAccessLevelHttpEntity = new HttpEntity<>(
                AccountConstants.UPDATE_LEVEL, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CHANGE_ACCESS_LEVEL +
                AccountConstants.JK_LOGIN), HttpMethod.PUT, changeAccessLevelHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(AccountConstants.UPDATE_LEVEL, accountService.getAccountByLogin(
                        AccountConstants.JK_LOGIN).getAccessLevel().getName()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.JK_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(14)
    @Test
    void confirmAccount() {

        HttpEntity<AccountCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountCreateRequest.builder()
                        .name(AccountConstants.NEW_NAME3)
                        .accessLevel(AccountConstants.NEW_ACCESS_LEVEL)
                        .email(AccountConstants.NEW_EMAIL3)
                        .surname(AccountConstants.NEW_SURNAME)
                        .build(), getHttpHeaders());

        ResponseEntity<String> responseCreate = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_OFFICE),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(responseCreate),
                () -> assertEquals(HttpStatus.OK, responseCreate.getStatusCode()),
                () -> assertEquals(AccountConstants.NEW_LOGIN3, urlRepository.findAll().get((int) urlRepository.count() - 1)
                        .getAccountEmployee().getLogin()),
                () -> assertFalse(accountService.getAccountByLogin(AccountConstants.NEW_LOGIN3).isConfirmed())
        );

        Url url = urlRepository.findAll().get((int) urlRepository.count() - 1);
        String combinedCode = url.getCodeDirector() + url.getCodeEmployee();

        HttpEntity<String> passwordHttpEntity = new HttpEntity<>(AccountConstants.NEW_PASSWORD, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CONFIRM + combinedCode)
                , HttpMethod.PUT, passwordHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.NEW_LOGIN3).isConfirmed()),
                () -> assertNotNull(accountService.getAccountByLogin(AccountConstants.NEW_LOGIN3).getPassword())
        );

    }

    @Order(15)
    @Test
    void resetPassword() {
        sendResetPasswordUrl();

        Url url = urlRepository.findAll().get((int) (urlRepository.count() - 1));
        int urlsCount = (int) urlRepository.count();

        String combinedCode = url.getCodeDirector() + url.getCodeEmployee();

        HttpEntity<String> resetRequestHttpEntity = new HttpEntity<>(AccountConstants.NEW_PASSWORD2, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.RESET_PASSWORD + combinedCode),
                HttpMethod.PUT, resetRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(urlsCount - 1, urlRepository.count()),
                () -> assertTrue(accountService.getAccountByLogin(AccountConstants.JK_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(16)
    @Test
    void sendResetPasswordUrl() {
        HttpEntity<ResetPasswordRequest> resetPasswordRequestHttpEntity = new HttpEntity<>(
                ResetPasswordRequest.builder()
                        .email(AccountConstants.JK_EMAIL)
                        .nameDirector(AccountConstants.SG_NAME)
                        .surnameDirector(AccountConstants.SG_SURNAME)
                        .build(), getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.SEND_RESET_PASSWORD),
                HttpMethod.POST, resetPasswordRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(AccountConstants.JK_LOGIN, urlRepository.findAll()
                        .get((int) (urlRepository.count() - 1)).getAccountEmployee().getLogin()),
                () -> assertEquals(AccountConstants.SG_LOGIN, urlRepository.findAll()
                        .get((int) (urlRepository.count() - 1)).getAccountDirector().getLogin()),
                () -> assertEquals(AccountConstants.URL_RESET_TYPE, urlRepository.findAll()
                        .get((int) (urlRepository.count() - 1)).getActionType())
        );
    }

    @Order(17)
    @Test
    void deleteUncofirmedAccount() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<AccountCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountCreateRequest.builder()
                        .name(AccountConstants.NEW_NAME)
                        .accessLevel(AccountConstants.NEW_ACCESS_LEVEL)
                        .email(AccountConstants.NEW_EMAIL5)
                        .surname(AccountConstants.NEW_SURNAME)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_OFFICE),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );

        int accountsCount = accountService.getAllAccounts().size();
        Account newAccount = accountService.getAllAccounts().get(accountsCount - 1);

        ResponseEntity<String> responseDelete = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS +
                        "/" + newAccount.getLogin()),
                HttpMethod.DELETE, getJwtHttpEntity(), String.class);

        assertAll(
                () -> assertNotNull(responseDelete),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(accountsCount - 1, accountService.getAllAccounts().size())
        );
    }
}
