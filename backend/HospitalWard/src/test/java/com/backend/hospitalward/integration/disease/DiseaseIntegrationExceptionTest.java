package com.backend.hospitalward.integration.disease;

import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.common.DiseaseConstants;
import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.request.disease.DiseaseUpdateRequest;
import com.backend.hospitalward.dto.response.exception.ExceptionResponse;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.integration.AbstractTestContainer;
import com.backend.hospitalward.model.Disease;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.DiseaseService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import liquibase.pro.packaged.T;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiseaseIntegrationExceptionTest extends AbstractTestContainer {

    @Autowired
    DiseaseService diseaseService;

    @Autowired
    TestRestTemplate restTemplate;

    Gson gson;
    String token;

    @BeforeEach
    public void authenticate() {
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(AccountConstants.SG_LOGIN, AccountConstants.SG_PASSWORD), null);
        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH), HttpMethod.POST,
                credentials, String.class);

        token = response.getBody();
    }

    @BeforeAll
    public void setUpGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd hh:mm:ss");
        gson = builder.create();
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
    void shouldReturn404WhenGetDiseaseByNameNotFound() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES +
                        "/NonExistingDisease"),
                HttpMethod.GET, jwtToken, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.DISEASE_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(2)
    @Test
    void shouldReturn404WhenUpdateDiseaseNotFound() {
        Disease disease = diseaseService.getAllDiseases().get(0);
        String diseaseName = disease.getLatinName();

        Long version = diseaseService.getDiseaseByName(diseaseName).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES +
                "/" + diseaseName), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<DiseaseUpdateRequest> diseaseUpdateRequestHttpEntity = new HttpEntity<>(
                DiseaseUpdateRequest.builder()
                        .latinName("UpdatedName")
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES
                        + "/NonExistingName"),
                HttpMethod.PUT, diseaseUpdateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.DISEASE_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(3)
    @Test
    void shouldReturn412WhenUpdateDiseaseInvalidVersion() {
        Disease disease = diseaseService.getAllDiseases().get(0);
        String diseaseName = disease.getLatinName();

        long version = diseaseService.getDiseaseByName(diseaseName).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES +
                "/" + diseaseName), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<DiseaseUpdateRequest> diseaseUpdateRequestHttpEntity = new HttpEntity<>(
                DiseaseUpdateRequest.builder()
                        .latinName("UpdatedName")
                        .version(version + 1)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES
                        + "/" + diseaseName),
                HttpMethod.PUT, diseaseUpdateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ETAG_INVALID, exceptionResponse.getMessage())
        );
    }

    @Order(4)
    @Test
    void shouldReturn412WhenUpdateDiseaseNoETag() {
        Disease disease = diseaseService.getAllDiseases().get(0);
        String diseaseName = disease.getLatinName();

        long version = diseaseService.getDiseaseByName(diseaseName).getVersion();

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, null);

        HttpEntity<DiseaseUpdateRequest> diseaseUpdateRequestHttpEntity = new HttpEntity<>(
                DiseaseUpdateRequest.builder()
                        .latinName("UpdatedName")
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES
                        + "/" + diseaseName),
                HttpMethod.PUT, diseaseUpdateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ETAG_INVALID, exceptionResponse.getMessage())
        );
    }

    @Order(5)
    @Test
    void shouldReturn409WhenUpdateDiseaseNameNotUnique() {
        Disease disease = diseaseService.getAllDiseases().get(0);
        Disease disease2 = diseaseService.getAllDiseases().get(1);
        String diseaseName = disease.getLatinName();

        long version = diseaseService.getDiseaseByName(diseaseName).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES +
                "/" + diseaseName), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<DiseaseUpdateRequest> diseaseUpdateRequestHttpEntity = new HttpEntity<>(
                DiseaseUpdateRequest.builder()
                        .latinName(disease2.getLatinName())
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES
                        + "/" + diseaseName),
                HttpMethod.PUT, diseaseUpdateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertTrue(exceptionResponse.getMessage().contains("disease.name"))
        );
    }

    @Order(6)
    @Test
    void shouldReturn404WhenDeleteDiseaseNotFound() {

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES
                        + "/NonExistingName"),
                HttpMethod.DELETE, getJwtHttpEntity(), String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals(ErrorKey.DISEASE_NOT_FOUND, exceptionResponse.getMessage())
        );
    }

    @Order(7)
    @Test
    void deleteDisease() {
        Disease disease = diseaseService.getAllDiseases().get(2);
        String diseaseName = disease.getLatinName();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES
                        + "/" + diseaseName),
                HttpMethod.DELETE, getJwtHttpEntity(), String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.DISEASE_ASSIGNED_TO_PATIENT, exceptionResponse.getMessage())
        );
    }
}
