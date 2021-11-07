package com.backend.hospitalward.integration;

import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.response.account.AccountGeneralDTO;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.AccountService;
import liquibase.pro.packaged.T;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountIntegrationTest extends AbstractTestContainer {

    @Autowired
    private AccountService accountService;

    @Autowired
    TestRestTemplate restTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    String token;

    @BeforeEach
    void setUp() throws IOException {
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials("sylwester.garda", "password"), null);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/auth"), HttpMethod.POST, credentials, String.class);

        token = response.getBody();


    }

    @Test
    @WithMockUser(username = "director", roles = "TREATMENT DIRECTOR")
    void getAllAccounts() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER + token);

        HttpEntity<T> jwtToken = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/accounts"), HttpMethod.GET, jwtToken, String.class);

        List<AccountGeneralDTO> accounts = objectMapper.readValue(response.getBody(), objectMapper.getTypeFactory()
                .constructCollectionType(List.class, AccountGeneralDTO.class));

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(8, accounts.size()),
                () -> assertNotNull(accounts.get(0)),
                () -> assertEquals("sylwester.garda", ((AccountGeneralDTO) accounts.get(0)).getLogin())
        );

    }

    @Test
    void getAccountByLogin() {
    }

    @Test
    void getProfile() {
    }

    @Test
    void createAccountOffice() {
    }

    @Test
    void createAccountMedic() {
    }

    @Test
    void changePassword() {
    }

    @Test
    void activateAccount() {
    }

    @Test
    void deactivateAccount() {
    }

    @Test
    void updateAccountOffice() {
    }

    @Test
    void updateAccountMedic() {
    }

    @Test
    void updateOwnAccountOffice() {
    }

    @Test
    void updateOwnAccountMedic() {
    }

    @Test
    void changeAccessLevel() {
    }

    @Test
    void confirmAccount() {
    }

    @Test
    void resetPassword() {
    }

    @Test
    void sendResetPasswordUrl() {
    }
}
