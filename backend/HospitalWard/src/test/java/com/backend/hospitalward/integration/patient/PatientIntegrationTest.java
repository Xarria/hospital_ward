package com.backend.hospitalward.integration.patient;

import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.common.DiseaseConstants;
import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.request.disease.DiseaseCreateRequest;
import com.backend.hospitalward.dto.request.patient.PatientCreateRequest;
import com.backend.hospitalward.integration.AbstractTestContainer;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.Queue;
import com.backend.hospitalward.repository.UrlRepository;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.AccountService;
import com.backend.hospitalward.service.PatientService;
import com.backend.hospitalward.service.QueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientIntegrationTest extends AbstractTestContainer {

    @Autowired
    PatientService patientService;
    @Autowired
    QueueService queueService;
    @Autowired
    TestRestTemplate restTemplate;
    Gson gson;
    String token;

    @BeforeEach
    public void authenticate() {
        String valid_password = AccountConstants.SG_PASSWORD;
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials(AccountConstants.SG_LOGIN, valid_password), null);
        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH), HttpMethod.POST,
                credentials, String.class);

        token = response.getBody();
    }

    @BeforeAll
    public void setUpGsonAndJsonMapper() {
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
    void getAllPatients() {
    }

    @Order(2)
    @Test
    void getPatientById() {
    }

    @Order(3)
    @Test
    void shouldCreateNewQueueWhenCreatePatientWithNonExistingDate() {
        int currentQueuesCount = queueService.getAllCurrentQueues().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Kowalska")
                        .age("4Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Katar"))
                        .admissionDate(LocalDate.of(2022, 5, 4))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .referralNr("12134234")
                        .sex("F")
                        .urgent(false).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        Patient newPatient = patientService.getPatientById((long) patientService.getAllPatients().size());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("UNDER_6", newPatient.getPatientType().getName()),
                () -> assertEquals("VACCINATED", newPatient.getCovidStatus().getStatus()),
                () -> assertEquals(0, newPatient.getPositionInQueue()),
                () -> assertEquals(currentQueuesCount + 1, queueService.getAllCurrentQueues().size()),
                () -> assertEquals(List.of(newPatient),
                        queueService.getQueueForDate(newPatient.getAdmissionDate()).getWaitingPatients())
        );
    }

    @Order(4)
    @Test
    void shouldAddPatientToExistingQueueWhenCreatePatient() {
        int currentQueuesCount = queueService.getAllCurrentQueues().size();
        Queue queue = queueService.getQueueForDate(LocalDate.of(2022, 3, 17));
        int patientsInQueueCount = queue.getPatients().size();
        int patientsWaitingCount = queue.getWaitingPatients().size();
        int patientConfirmedCount = queue.getConfirmedPatients().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Nowak")
                        .age("7Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Katar"))
                        .admissionDate(LocalDate.of(2022, 3, 17))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .referralNr("121574234")
                        .sex("F")
                        .urgent(false).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        Patient newPatient = patientService.getPatientById((long) patientService.getAllPatients().size());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("GIRL", newPatient.getPatientType().getName()),
                () -> assertEquals("VACCINATED", newPatient.getCovidStatus().getStatus()),
                () -> assertEquals(2, newPatient.getPositionInQueue()),
                () -> assertEquals(currentQueuesCount, queueService.getAllCurrentQueues().size()),
                () -> assertTrue(queueService.getQueueForDate(newPatient.getAdmissionDate()).getPatients()
                        .contains(newPatient)),
                () -> assertEquals(patientsInQueueCount + 1, queueService.getQueueForDate(
                        newPatient.getAdmissionDate()).getPatients().size()),
                () -> assertEquals(patientsWaitingCount + 1, queueService.getQueueForDate(
                        newPatient.getAdmissionDate()).getWaitingPatients().size()),
                () -> assertEquals(patientConfirmedCount, queueService.getQueueForDate(
                        newPatient.getAdmissionDate()).getConfirmedPatients().size())
        );
    }

    @Test
    void shouldCreateUrgentPatientWhenQueueLocked() {
        int currentQueuesCount = queueService.getAllCurrentQueues().size();
        Queue queue = queueService.getQueueForDate(LocalDate.of(2022, 3, 15));
        int patientsInQueueCount = queue.getPatients().size();
        int patientsWaitingCount = queue.getWaitingPatients().size();
        int patientConfirmedCount = queue.getConfirmedPatients().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Nowak")
                        .age("7Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Katar"))
                        .admissionDate(LocalDate.of(2022, 3, 15))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .referralNr("12159908984")
                        .sex("F")
                        .urgent(true).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/urgent"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        Patient newPatient = patientService.getPatientById((long) patientService.getAllPatients().size());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("URGENT", newPatient.getPatientType().getName()),
                () -> assertEquals("VACCINATED", newPatient.getCovidStatus().getStatus()),
                () -> assertEquals(2, newPatient.getPositionInQueue()),
                () -> assertEquals(currentQueuesCount, queueService.getAllCurrentQueues().size()),
                () -> assertTrue(queueService.getQueueForDate(newPatient.getAdmissionDate()).getPatients()
                        .contains(newPatient)),
                () -> assertEquals(patientsInQueueCount + 1, queueService.getQueueForDate(
                        newPatient.getAdmissionDate()).getPatients().size()),
                () -> assertEquals(patientsWaitingCount + 1, queueService.getQueueForDate(
                        newPatient.getAdmissionDate()).getWaitingPatients().size()),
                () -> assertEquals(patientConfirmedCount, queueService.getQueueForDate(
                        newPatient.getAdmissionDate()).getConfirmedPatients().size())
        );
    }

    @Test
    void updatePatient() {
    }

    @Test
    void confirmPatient() {
    }

    @Test
    void changeAdmissionDate() {
    }

    @Test
    void changeUrgency() {
    }

    @Test
    void deletePatient() {
    }
}
