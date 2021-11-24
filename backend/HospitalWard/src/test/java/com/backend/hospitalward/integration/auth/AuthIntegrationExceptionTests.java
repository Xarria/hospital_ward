package com.backend.hospitalward.integration.auth;

import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.integration.AbstractTestContainer;
import com.backend.hospitalward.integration.common.AccountConstants;
import com.backend.hospitalward.service.AccountService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthIntegrationExceptionTests extends AbstractTestContainer {

    @Autowired
    AccountService accountService;

    @Autowired
    RestTemplate restTemplate;


    @Order(1)
    @Test
    public void authenticateBadCredentials() {

        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(AccountConstants.SG_LOGIN, AccountConstants.NEW_PASSWORD2), null);
        try {
            restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH), HttpMethod.POST, credentials, String.class);
        } catch (HttpClientErrorException e) {
            assertAll(
                    () -> assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode())
            );
        }
    }
}
