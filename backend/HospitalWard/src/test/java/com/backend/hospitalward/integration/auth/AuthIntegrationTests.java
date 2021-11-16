package com.backend.hospitalward.integration.auth;

import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.integration.AbstractTestContainer;
import com.backend.hospitalward.integration.common.AccountConstants;
import com.backend.hospitalward.service.AccountService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthIntegrationTests extends AbstractTestContainer {

    @Autowired
    AccountService accountService;

    @Autowired
    TestRestTemplate restTemplate;


    @Order(1)
    @Test
    public void authenticate() {
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(AccountConstants.SG_LOGIN, AccountConstants.SG_PASSWORD), null);
        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH), HttpMethod.POST, credentials, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(response.getBody().matches(".*\\..*\\..*"))
        );
    }
}
