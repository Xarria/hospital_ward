package com.backend.hospitalward.integration.account;

import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.response.account.AccountGeneralResponse;
import com.backend.hospitalward.dto.response.medicalStaff.MedicalStaffGeneralResponse;
import com.backend.hospitalward.integration.AbstractTestContainer;
import com.backend.hospitalward.integration.common.AccountConstants;
import com.backend.hospitalward.repository.UrlRepository;
import com.backend.hospitalward.service.AccountService;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountIntegrationExceptionTests extends AbstractTestContainer {

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
}
