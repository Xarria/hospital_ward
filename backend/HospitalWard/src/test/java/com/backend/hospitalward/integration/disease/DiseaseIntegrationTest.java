package com.backend.hospitalward.integration.disease;

import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.common.DiseaseConstants;
import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.request.disease.DiseaseCreateRequest;
import com.backend.hospitalward.dto.request.disease.DiseaseUpdateRequest;
import com.backend.hospitalward.dto.response.disease.DiseaseDetailsResponse;
import com.backend.hospitalward.dto.response.disease.DiseaseGeneralResponse;
import com.backend.hospitalward.exception.NotFoundException;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiseaseIntegrationTest extends AbstractTestContainer {

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

    @NotNull
    private Timestamp getTimestampToCompare() {
        return Timestamp.from(Instant.now().minusSeconds(5));
    }

    @Order(1)
    @Test
    void getAllDiseases() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES),
                HttpMethod.GET, jwtToken, String.class);

        List<DiseaseGeneralResponse> diseases = Arrays.asList(gson.fromJson(response.getBody(),
                DiseaseGeneralResponse[].class));

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(diseaseService.getAllDiseases().size(), diseases.size()),
                () -> assertNotNull(diseases.get(0)),
                () -> assertEquals(diseaseService.getAllDiseases().get(0).getName(), diseases.get(0).getName())
        );
    }

    @Order(2)
    @Test
    void getDiseaseByName() {
        HttpEntity<T> jwtToken = getJwtHttpEntity();

        Disease disease = diseaseService.getAllDiseases().get(0);
        String diseaseName = disease.getName();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES +
                        "/" + diseaseName),
                HttpMethod.GET, jwtToken, String.class);

        DiseaseDetailsResponse diseaseDetailsResponse = gson.fromJson(response.getBody(), DiseaseDetailsResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(disease.getCreationDate(), diseaseDetailsResponse.getCreationDate())
        );
    }

    @Order(3)
    @Test
    void createDisease() {
        HttpEntity<DiseaseCreateRequest> diseaseCreateRequestHttpEntity = new HttpEntity<>(
                DiseaseCreateRequest.builder()
                        .name("DiseaseName")
                        .cathererRequired(true)
                        .surgeryRequired(false)
                        .build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES),
                HttpMethod.POST, diseaseCreateRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(diseaseService.getDiseaseByName("DiseaseName")),
                () -> assertTrue(diseaseService.getDiseaseByName("DiseaseName").isCathererRequired()),
                () -> assertFalse(diseaseService.getDiseaseByName("DiseaseName").isSurgeryRequired())
        );
    }

    @Order(4)
    @Test
    void updateDisease() {
        Disease disease = diseaseService.getAllDiseases().get(0);
        String diseaseName = disease.getName();

        Long version = diseaseService.getDiseaseByName(diseaseName).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES +
                "/" + diseaseName), HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        HttpEntity<DiseaseUpdateRequest> diseaseUpdateRequestHttpEntity = new HttpEntity<>(
                DiseaseUpdateRequest.builder()
                        .name("UpdatedName")
                        .version(version)
                        .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES
                        + "/" + diseaseName),
                HttpMethod.PUT, diseaseUpdateRequestHttpEntity, String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(diseaseService.getDiseaseByName("UpdatedName")),
                () -> assertTrue(diseaseService.getDiseaseByName("UpdatedName").getModificationDate()
                        .after(getTimestampToCompare()))
        );
    }

    @Order(5)
    @Test
    void deleteDisease() {
        HttpEntity<DiseaseCreateRequest> diseaseCreateRequestHttpEntity = new HttpEntity<>(
                DiseaseCreateRequest.builder()
                        .name("DiseaseName2")
                        .cathererRequired(true)
                        .surgeryRequired(false)
                        .build(), getHttpHeaders()
        );

        ResponseEntity<String> responseCreate = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES),
                HttpMethod.POST, diseaseCreateRequestHttpEntity, String.class);


        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(DiseaseConstants.GET_ALL_DISEASES
                        + "/" + "DiseaseName2"),
                HttpMethod.DELETE, getJwtHttpEntity(), String.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertThrows(NotFoundException.class, () -> diseaseService.getDiseaseByName("DiseaseName2"))
        );
    }
}
