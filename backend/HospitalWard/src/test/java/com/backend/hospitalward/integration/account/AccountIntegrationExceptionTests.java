package com.backend.hospitalward.integration.account;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import com.backend.hospitalward.dto.request.account.ChangePasswordRequest;
import com.backend.hospitalward.dto.request.account.ResetPasswordRequest;
import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffUpdateRequest;
import com.backend.hospitalward.dto.response.exception.ExceptionResponse;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.model.Url;
import com.backend.hospitalward.repository.UrlRepository;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.AccountService;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import liquibase.pro.packaged.T;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AccountIntegrationExceptionTests.DockerMysqlDataSourceInitializer.class)
@Testcontainers
@FieldDefaults(level = AccessLevel.PRIVATE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountIntegrationExceptionTests {

    @Autowired
    AccountService accountService;

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    TestRestTemplate restTemplate;

    Gson gson;

    String token;

    ////////////////////////
    static MySQLContainer<?> mySQLContainer;

    static {
        mySQLContainer = new MySQLContainer<>("mysql:8.0.26");
        mySQLContainer.start();
    }

    @LocalServerPort
    int port;

    public String getUrlWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    public static class DockerMysqlDataSourceInitializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.password=" + mySQLContainer.getPassword()
            );
        }
    }

    ///////////////////////////////////

    @BeforeEach
    public void authenticate() {
        String valid_password = AccountConstants.SG_PASSWORD;
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(AccountConstants.SG_LOGIN, valid_password), null);
        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH), HttpMethod.POST, credentials, String.class);

        token = response.getBody();
    }

    @BeforeAll
    public void setUpGson() {
        GsonFireBuilder builder = new GsonFireBuilder();
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

    @Order(1)
    @Test
    void shouldReturn404WhenGetAccountByLoginNotFound() {

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS +
                "/" + "NonExistingLogin"), HttpMethod.GET, getJwtHttpEntity(), String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(2)
    @Test
    void shouldReturn403WhenGetProfileWithoutJWT() {

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_PROFILE),
                HttpMethod.GET, null, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode())
        );
    }

    @Order(3)
    @Test
    void shouldReturn409WhenCreateAccountEmailNotUnique() {
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
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );

        ResponseEntity<String> response2 = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_OFFICE),
                HttpMethod.POST, createRequestHttpEntity, String.class);


        ExceptionResponse exceptionResponse = gson.fromJson(response2.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response2),
                () -> assertEquals(HttpStatus.CONFLICT, response2.getStatusCode()),
                () -> assertTrue(exceptionResponse.getMessage().contains("account.email"))
        );
    }

    @Order(4)
    @Test
    void shouldReturn400WhenCreateMedicWithInvalidLicenseNr() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<MedicalStaffCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffCreateRequest.builder()
                        .name(AccountConstants.NEW_NAME2)
                        .accessLevel(AccountConstants.NURSE)
                        .email(AccountConstants.NEW_EMAIL2)
                        .surname(AccountConstants.NEW_SURNAME)
                        .licenseNr(AccountConstants.NEW_LICENSE_NR)
                        .academicDegree(AccountConstants.NEW_DEGREE)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_MEDIC),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(ErrorKey.LICENSE_NUMBER, exceptionResponse.getMessage())
        );
    }

    @Order(5)
    @Test
    void shouldReturn404WhenCreateMedicWithInvalidAccessLevel() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<MedicalStaffCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffCreateRequest.builder()
                        .name(AccountConstants.NEW_NAME2)
                        .accessLevel("NonExistingLevel")
                        .email(AccountConstants.NEW_EMAIL3)
                        .surname(AccountConstants.NEW_SURNAME)
                        .licenseNr(AccountConstants.NEW_LICENSE_NR)
                        .academicDegree(AccountConstants.NEW_DEGREE)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_MEDIC),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ACCESS_LEVEL_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(6)
    @Test
    void shouldReturn400WhenCreateMedicWithOfficeAccessLevel() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<MedicalStaffCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                MedicalStaffCreateRequest.builder()
                        .name(AccountConstants.NEW_NAME2)
                        .accessLevel(AccountConstants.NEW_ACCESS_LEVEL)
                        .email(AccountConstants.NEW_EMAIL3)
                        .surname(AccountConstants.NEW_SURNAME)
                        .licenseNr(AccountConstants.NEW_LICENSE_NR)
                        .academicDegree(AccountConstants.NEW_DEGREE)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_MEDIC),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);


        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ACCESS_LEVEL_INVALID_MEDIC, exceptionResponse.getMessage())
        );
    }

    @Order(7)
    @Test
    void shouldReturn400WhenBadOldPassword() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<ChangePasswordRequest> changePasswordRequestHttpEntity = new HttpEntity<>(
                ChangePasswordRequest.builder()
                        .oldPassword(AccountConstants.NEW_SG_PASSWORD)
                        .newPassword(AccountConstants.NEW_SG_PASSWORD)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CHANGE_PASSWORD),
                HttpMethod.PUT, changePasswordRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);


        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(ErrorKey.PASSWORD_INCORRECT, exceptionResponse.getMessage())
        );
    }

    @Order(8)
    @Test
    void shouldReturn409WhenNewPasswordSameAsOldPassword() {
        HttpHeaders headers = getHttpHeaders();

        HttpEntity<ChangePasswordRequest> changePasswordRequestHttpEntity = new HttpEntity<>(
                ChangePasswordRequest.builder()
                        .oldPassword(AccountConstants.SG_PASSWORD)
                        .newPassword(AccountConstants.SG_PASSWORD)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CHANGE_PASSWORD),
                HttpMethod.PUT, changePasswordRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);


        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.PASSWORD_THE_SAME, exceptionResponse.getMessage())
        );
    }

    @Order(9)
    @Test
    void shouldReturn404WhenAccountDeactivateNotFound() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.DEACTIVATE
                + "NonExistingLogin"), HttpMethod.PUT, jwtToken, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(10)
    @Test
    void shouldReturn412WhenUpdateWithoutEtag() {

        Long version = accountService.getAccountByLogin(
                AccountConstants.OFFICE_LOGIN).getVersion();

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, null);

        HttpEntity<AccountUpdateRequest> updateRequestHttpEntity = new HttpEntity<>(
                AccountUpdateRequest.builder()
                        .login(AccountConstants.OFFICE_LOGIN)
                        .name(AccountConstants.UPDATE_NAME)
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.UPDATE_OFFICE
                + AccountConstants.OFFICE_LOGIN), HttpMethod.PUT, updateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ETAG_INVALID, exceptionResponse.getMessage())
        );
    }

    @Order(11)
    @Test
    void shouldReturn412WhenUpdateWithWrongVersion() {

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS +
                "/" + AccountConstants.SG_LOGIN), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<AccountUpdateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountUpdateRequest.builder()
                        .login(AccountConstants.OFFICE_LOGIN)
                        .name(AccountConstants.UPDATE_NAME)
                        .version(4566L)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.UPDATE_OFFICE
                + AccountConstants.OFFICE_LOGIN), HttpMethod.PUT, createRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ETAG_INVALID, exceptionResponse.getMessage())
        );
    }

    @Order(12)
    @Test
    void updateReturn404WhenUpdateWithInvalidSpecialization() {

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
                        .specializations(List.of(AccountConstants.UPDATE_LICENSE_NR))
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.UPDATE_MEDIC
                + AccountConstants.SG_LOGIN), HttpMethod.PUT, createRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.SPECIALIZATION_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(13)
    @Test
    void shouldReturn404WhenChangeAccessLevelAccountNotFound() {

        HttpEntity<String> changeAccessLevelHttpEntity = new HttpEntity<>(
                AccountConstants.UPDATE_LEVEL, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CHANGE_ACCESS_LEVEL +
                "NonExistingLogin"), HttpMethod.PUT, changeAccessLevelHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ACCOUNT_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(14)
    @Test
    void shouldReturn409WhenChangeAccessLevelOffice() {

        HttpEntity<String> changeAccessLevelHttpEntity = new HttpEntity<>(
                AccountConstants.UPDATE_LEVEL, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CHANGE_ACCESS_LEVEL +
                AccountConstants.OFFICE_LOGIN), HttpMethod.PUT, changeAccessLevelHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.OFFICE_STAFF_ACCESS_LEVEL_CHANGE, exceptionResponse.getMessage())
        );
    }

    @Order(15)
    @Test
    void shouldReturn409WhenChangeAccessLevelMedicToOffice() {

        HttpEntity<String> changeAccessLevelHttpEntity = new HttpEntity<>(
                AccountConstants.SECRETARY, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CHANGE_ACCESS_LEVEL +
                AccountConstants.JK_LOGIN), HttpMethod.PUT, changeAccessLevelHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.MEDICAL_STAFF_TO_OFFICE_CHANGE, exceptionResponse.getMessage())
        );
    }

    @Order(16)
    @Test
    void shouldReturn400WhenConfirmWithUrlBadActionType() {

        createAccount(AccountConstants.NEW_EMAIL3);

        Url url = urlRepository.findAll().get((int) urlRepository.count() - 1);
        url.setActionType("Wrong");
        urlRepository.save(url);
        String combinedCode = url.getCodeDirector() + url.getCodeEmployee();

        HttpEntity<String> passwordHttpEntity = new HttpEntity<>(AccountConstants.NEW_PASSWORD, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CONFIRM + combinedCode)
                , HttpMethod.PUT, passwordHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(ErrorKey.URL_WRONG_ACTION, exceptionResponse.getMessage())
        );

    }

    @Order(17)
    @Test
    void shouldReturn405WhenConfirmWithUrlExpired() {

        createAccount(AccountConstants.NEW_EMAIL2);

        Url url = urlRepository.findAll().get((int) urlRepository.count() - 1);
        url.setExpirationDate(Timestamp.from(Instant.now().minusSeconds(10)));
        urlRepository.save(url);
        String combinedCode = url.getCodeDirector() + url.getCodeEmployee();

        HttpEntity<String> passwordHttpEntity = new HttpEntity<>(AccountConstants.NEW_PASSWORD, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CONFIRM + combinedCode)
                , HttpMethod.PUT, passwordHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.GONE, response.getStatusCode()),
                () -> assertEquals(ErrorKey.URL_EXPIRED, exceptionResponse.getMessage())
        );

    }

    @Order(18)
    @Test
    void shouldReturn404WhenConfirmWithUrlInvalidCode() {

        createAccount(AccountConstants.NEW_EMAIL4);

        String combinedCode = "WRONG_CODE";

        HttpEntity<String> passwordHttpEntity = new HttpEntity<>(AccountConstants.NEW_PASSWORD, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CONFIRM + combinedCode)
                , HttpMethod.PUT, passwordHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.URL_NOT_FOUND, exceptionResponse.getMessage())
        );

    }

    @Order(19)
    @Test
    void shouldReturn409WhenConfirmWithUrlInvalidCodeLength() {

        createAccount(AccountConstants.NEW_EMAIL5);

        String combinedCode = "WRONG_LENGTH_CODE";

        HttpEntity<String> passwordHttpEntity = new HttpEntity<>(AccountConstants.NEW_PASSWORD, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.CONFIRM + combinedCode)
                , HttpMethod.PUT, passwordHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(ErrorKey.URL_INVALID, exceptionResponse.getMessage())
        );

    }

    @Order(20)
    @Test
    void shouldReturn404WhenResetPasswordUrlInvalid() {
        sendResetPasswordUrl();

        String combinedCode = "WRONG_CODE";

        HttpEntity<String> resetRequestHttpEntity = new HttpEntity<>(AccountConstants.NEW_PASSWORD2, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.RESET_PASSWORD + combinedCode),
                HttpMethod.PUT, resetRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.URL_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(21)
    @Test
    void shouldReturn405WhenResetPasswordUrlExpired() {
        sendResetPasswordUrl();

        Url url = urlRepository.findAll().get((int) (urlRepository.count() - 1));

        url.setExpirationDate(Timestamp.from(Instant.now().minusSeconds(10)));
        urlRepository.save(url);
//        urlRepository.flush();
        String combinedCode = url.getCodeDirector() + url.getCodeEmployee();

        HttpEntity<String> resetRequestHttpEntity = new HttpEntity<>(AccountConstants.NEW_PASSWORD2, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.RESET_PASSWORD + combinedCode),
                HttpMethod.PUT, resetRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.GONE, response.getStatusCode()),
                () -> assertEquals(ErrorKey.URL_EXPIRED, exceptionResponse.getMessage())
        );
    }

    @Order(22)
    @Test
    void shouldReturn409WhenResetPasswordSameAsOldPassword() {
        sendResetPasswordUrl();

        Url url = urlRepository.findAll().get((int) (urlRepository.count() - 1));
        String combinedCode = url.getCodeDirector() + url.getCodeEmployee();

        HttpEntity<String> resetRequestHttpEntity = new HttpEntity<>(AccountConstants.SG_PASSWORD, getHttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.RESET_PASSWORD + combinedCode),
                HttpMethod.PUT, resetRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ERROR_SAME_PASSWORD, exceptionResponse.getMessage())
        );
    }

    @Order(23)
    @Test
    void shouldReturn409WhenAccountActivateUnconfirmed() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        createAccount(AccountConstants.NEW_EMAIL6);

        String newAccountLogin = accountService.getAllAccounts().get(accountService.getAllAccounts().size() - 1).getLogin();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.DEACTIVATE
                + newAccountLogin), HttpMethod.PUT, jwtToken, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ACCOUNT_NOT_CONFIRMED, exceptionResponse.getMessage())
        );
    }

    @Order(24)
    @Test
    void shouldReturn409WhenDeleteConfirmedAccount() {
        ResponseEntity<String> responseDelete = restTemplate.exchange(getUrlWithPort(AccountConstants.GET_ALL_ACCOUNTS +
                        "/" + AccountConstants.JK_LOGIN),
                HttpMethod.DELETE, getJwtHttpEntity(), String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(responseDelete.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(responseDelete),
                () -> assertEquals(HttpStatus.CONFLICT, responseDelete.getStatusCode()),
                () -> assertEquals(ErrorKey.ACCOUNT_CONFIRMED, exceptionResponse.getMessage())
        );
    }

    private void createAccount(String newEmail6) {
        HttpEntity<AccountCreateRequest> createRequestHttpEntity = new HttpEntity<>(
                AccountCreateRequest.builder()
                        .name(AccountConstants.NEW_NAME3)
                        .accessLevel(AccountConstants.NEW_ACCESS_LEVEL)
                        .email(newEmail6)
                        .surname(AccountConstants.NEW_SURNAME)
                        .build(), getHttpHeaders());

        ResponseEntity<String> responseCreate = restTemplate.exchange(getUrlWithPort(AccountConstants.CREATE_OFFICE),
                HttpMethod.POST, createRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(responseCreate),
                () -> assertEquals(HttpStatus.OK, responseCreate.getStatusCode())
        );
    }

    private void sendResetPasswordUrl() {
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
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

}
