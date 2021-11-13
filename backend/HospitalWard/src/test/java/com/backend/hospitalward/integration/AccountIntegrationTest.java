package com.backend.hospitalward.integration;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import com.backend.hospitalward.dto.request.account.ChangePasswordRequest;
import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffUpdateRequest;
import com.backend.hospitalward.dto.response.account.AccountGeneralDTO;
import com.backend.hospitalward.dto.response.medicalStaff.MedicalStaffGeneralDTO;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountIntegrationTest extends AbstractTestContainer {

    @Autowired
    AccountService accountService;

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    TestRestTemplate restTemplate;

    Gson gson;

    String token;

    String valid_password = TestConstants.SG_PASSWORD;

    @BeforeEach
    void setUp() {

        setUpGson();

        authenticate();

    }

    private void authenticate() {
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(TestConstants.SG_LOGIN, valid_password), null);
        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.AUTH), HttpMethod.POST, credentials, String.class);

        token = response.getBody();
    }

    private void setUpGson() {
        GsonFireBuilder builder = new GsonFireBuilder()
                .registerTypeSelector(AccountGeneralDTO.class, readElement -> {
                    String type = readElement.getAsJsonObject().get(TestConstants.GSON_TYPE_IDENTIFIER).getAsString();
                    if (type.equals(TestConstants.TYPE_MEDIC)) {
                        return MedicalStaffGeneralDTO.class;
                    } else if (type.equals(TestConstants.TYPE_OFFICE)) {
                        return AccountGeneralDTO.class;
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

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.GET_ALL_ACCOUNTS),
                HttpMethod.GET, jwtToken, String.class);

        List<AccountGeneralDTO> accounts = Arrays.asList(gson.fromJson(response.getBody(), AccountGeneralDTO[].class));

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(accountService.getAllAccounts().size(), accounts.size()),
                () -> assertNotNull(accounts.get(0)),
                () -> assertEquals(TestConstants.SG_LOGIN, accounts.get(0).getLogin())
        );

    }

    @Order(2)
    @Test
    void getAccountByLogin() {

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.GET_ALL_ACCOUNTS +
                        "/" + TestConstants.SG_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        AccountGeneralDTO account = gson.fromJson(response.getBody(), AccountGeneralDTO.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getHeaders().get(HttpHeaders.ETAG)),
                () -> assertNotNull(account),
                () -> assertEquals(TestConstants.SG_LOGIN, account.getLogin()),
                () -> assertEquals(TestConstants.SG_LICENSE_NR, ((MedicalStaffGeneralDTO) account).getLicenseNr())
        );
    }

    @Order(3)
    @Test
    void getProfile() {

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.GET_PROFILE),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        AccountGeneralDTO account = gson.fromJson(response.getBody(), AccountGeneralDTO.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getHeaders().get(HttpHeaders.ETAG)),
                () -> assertNotNull(account),
                () -> assertEquals(TestConstants.SG_LOGIN, account.getLogin()),
                () -> assertEquals(TestConstants.SG_LICENSE_NR, ((MedicalStaffGeneralDTO) account).getLicenseNr())
        );
    }

    @Order(4)
    @Test
    void createAccountOffice() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<AccountCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountCreateRequest.builder()
                .name(TestConstants.NEW_NAME)
                .accessLevel(TestConstants.NEW_ACCESS_LEVEL)
                .email(TestConstants.NEW_EMAIL)
                .password(TestConstants.NEW_PASSWORD)
                .surname(TestConstants.NEW_SURNAME)
                .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.CREATE_OFFICE),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(accountService.getAccountByLogin(TestConstants.NEW_LOGIN1)),
                () -> assertEquals(TestConstants.NEW_LOGIN1, urlRepository.findAll().get((int) urlRepository.count() - 1)
                        .getAccountEmployee().getLogin()),
                () -> assertEquals(TestConstants.NEW_ACCESS_LEVEL, accountService.getAccountByLogin(
                        TestConstants.NEW_LOGIN1).getAccessLevel().getName()),
                () -> assertEquals(TestConstants.TYPE_OFFICE, accountService.getAccountByLogin(
                        TestConstants.NEW_LOGIN1).getType()),
                () -> assertEquals(TestConstants.SG_LOGIN, accountService.getAccountByLogin(TestConstants.NEW_LOGIN1)
                        .getCreatedBy().getLogin())
        );
    }

    @Order(5)
    @Test
    void createAccountMedic() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<MedicalStaffCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffCreateRequest.builder()
                        .name(TestConstants.NEW_NAME2)
                        .accessLevel(TestConstants.NEW_ACCESS_LEVEL2)
                        .email(TestConstants.NEW_EMAIL2)
                        .password(TestConstants.NEW_PASSWORD)
                        .surname(TestConstants.NEW_SURNAME)
                        .licenseNr(TestConstants.NEW_LICENSE_NR)
                        .academicDegree(TestConstants.NEW_DEGREE)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.CREATE_MEDIC),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(accountService.getAccountByLogin(TestConstants.NEW_LOGIN2)),
                () -> assertEquals(TestConstants.NEW_LOGIN2, urlRepository.findAll().get((int) urlRepository.count() - 1)
                        .getAccountEmployee().getLogin()),
                () -> assertEquals(TestConstants.NEW_ACCESS_LEVEL2, accountService.getAccountByLogin(
                        TestConstants.NEW_LOGIN2).getAccessLevel().getName()),
                () -> assertEquals(TestConstants.TYPE_MEDIC, accountService.getAccountByLogin(
                        TestConstants.NEW_LOGIN2).getType()),
                () -> assertEquals(TestConstants.SG_LOGIN, accountService.getAccountByLogin(TestConstants.NEW_LOGIN2)
                        .getCreatedBy().getLogin())
        );
    }

    @Order(6)
    @Test
    void changePassword() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<ChangePasswordRequest> changePasswordRequestHttpEntity = new HttpEntity<>(
                ChangePasswordRequest.builder()
                        .oldPassword(TestConstants.SG_PASSWORD)
                        .newPassword(TestConstants.NEW_SG_PASSWORD)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.CHANGE_PASSWORD),
                HttpMethod.PUT, changePasswordRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.SG_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );

        valid_password = TestConstants.NEW_SG_PASSWORD;
    }



    @Order(7)
    @Test
    void deactivateAccount() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.DEACTIVATE
                        + TestConstants.JK_LOGIN), HttpMethod.PUT, jwtToken, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertFalse(accountService.getAccountByLogin(TestConstants.JK_LOGIN).isActive()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.JK_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(8)
    @Test
    void activateAccount() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.ACTIVATE
                + TestConstants.JK_LOGIN), HttpMethod.PUT, jwtToken, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.JK_LOGIN).isActive()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.JK_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(9)
    @Test
    void updateAccountOffice() {

        Long version = accountService.getAccountByLogin(
                TestConstants.OFFICE_LOGIN).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(TestConstants.GET_ALL_ACCOUNTS +
                "/" + TestConstants.OFFICE_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<AccountUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountUpdateRequest.builder()
                        .login(TestConstants.OFFICE_LOGIN)
                        .name(TestConstants.UPDATE_NAME)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.UPDATE_OFFICE
                        + TestConstants.OFFICE_LOGIN), HttpMethod.PUT, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(TestConstants.UPDATE_NAME, accountService.getAccountByLogin(
                        TestConstants.OFFICE_LOGIN).getName()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.OFFICE_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(10)
    @Test
    void updateAccountMedic() {

        Long version = accountService.getAccountByLogin(
                TestConstants.SG_LOGIN).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(TestConstants.GET_ALL_ACCOUNTS +
                "/" + TestConstants.SG_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<MedicalStaffUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffUpdateRequest.builder()
                        .login(TestConstants.SG_LOGIN)
                        .name(TestConstants.UPDATE_NAME)
                        .licenseNr(TestConstants.UPDATE_LICENSE_NR)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.UPDATE_MEDIC
                + TestConstants.SG_LOGIN), HttpMethod.PUT, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(TestConstants.UPDATE_NAME, accountService.getAccountByLogin(
                        TestConstants.SG_LOGIN).getName()),
                () -> assertEquals(TestConstants.UPDATE_LICENSE_NR, ((MedicalStaff) accountService.getAccountByLogin(
                        TestConstants.SG_LOGIN)).getLicenseNr()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.SG_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(11)
    @Test
    void updateOwnAccountOffice() {
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(TestConstants.OFFICE_LOGIN, TestConstants.SG_PASSWORD), null);
        ResponseEntity<String> responseAuth = restTemplate.exchange(getUrlWithPort(TestConstants.AUTH),
                HttpMethod.POST, credentials, String.class);

        token = responseAuth.getBody();

        Long version = accountService.getAccountByLogin(
                TestConstants.OFFICE_LOGIN).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(TestConstants.GET_ALL_ACCOUNTS +
                "/" + TestConstants.OFFICE_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<AccountUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountUpdateRequest.builder()
                        .login(TestConstants.OFFICE_LOGIN)
                        .surname(TestConstants.UPDATE_NAME)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.UPDATE_OWN_OFFICE)
                , HttpMethod.PUT, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(TestConstants.UPDATE_NAME, accountService.getAccountByLogin(
                        TestConstants.OFFICE_LOGIN).getSurname()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.OFFICE_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(12)
    @Test
    void updateOwnAccountMedic() {

        Long version = accountService.getAccountByLogin(
                TestConstants.SG_LOGIN).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(TestConstants.GET_ALL_ACCOUNTS +
                "/" + TestConstants.SG_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<MedicalStaffUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffUpdateRequest.builder()
                        .login(TestConstants.SG_LOGIN)
                        .surname(TestConstants.UPDATE_NAME)
                        .licenseNr(TestConstants.UPDATE_LICENSE_NR2)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.UPDATE_OWN_MEDIC)
                , HttpMethod.PUT, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(TestConstants.UPDATE_NAME, accountService.getAccountByLogin(
                        TestConstants.SG_LOGIN).getSurname()),
                () -> assertEquals(TestConstants.UPDATE_LICENSE_NR2, ((MedicalStaff) accountService.getAccountByLogin(
                        TestConstants.SG_LOGIN)).getLicenseNr()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.SG_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(13)
    @Test
    void changeAccessLevel() {

        HttpEntity<String> changeAccessLevelHttpEntity = new HttpEntity<>(
                TestConstants.UPDATE_LEVEL, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.CHANGE_ACCESS_LEVEL +
                        TestConstants.JK_LOGIN), HttpMethod.PUT, changeAccessLevelHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(TestConstants.UPDATE_LEVEL, accountService.getAccountByLogin(
                        TestConstants.JK_LOGIN).getAccessLevel().getName()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.JK_LOGIN).getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(14)
    @Test
    void confirmAccount() {

        HttpEntity<AccountCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountCreateRequest.builder()
                        .name(TestConstants.NEW_NAME3)
                        .accessLevel(TestConstants.NEW_ACCESS_LEVEL)
                        .email(TestConstants.NEW_EMAIL3)
                        .password(TestConstants.NEW_PASSWORD)
                        .surname(TestConstants.NEW_SURNAME)
                        .build(), getHttpHeaders());

        ResponseEntity<String> responseCreate = restTemplate.exchange(getUrlWithPort(TestConstants.CREATE_OFFICE),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(responseCreate),
                () -> assertEquals(HttpStatus.OK, responseCreate.getStatusCode()),
                () -> assertEquals(TestConstants.NEW_LOGIN3, urlRepository.findAll().get((int) urlRepository.count() - 1)
                        .getAccountEmployee().getLogin()),
                () -> assertFalse(accountService.getAccountByLogin(TestConstants.NEW_LOGIN3).isConfirmed())
        );

        Url url = urlRepository.findAll().get((int) urlRepository.count() - 1);
        String combinedCode = url.getCodeDirector() + url.getCodeEmployee();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(TestConstants.CONFIRM + combinedCode)
                , HttpMethod.PUT, getJwtHttpEntity(), String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(accountService.getAccountByLogin(TestConstants.NEW_LOGIN3).isConfirmed())
        );

    }

    @Order(15)
    @Test
    void resetPassword() {
    }

    @Order(16)
    @Test
    void sendResetPasswordUrl() {
    }
}
