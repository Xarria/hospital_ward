package com.backend.hospitalward.integration.patient;

import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.request.patient.PatientCreateRequest;
import com.backend.hospitalward.dto.response.exception.ExceptionResponse;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.PatientService;
import com.backend.hospitalward.service.QueueService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PatientIntegrationExceptionTest.DockerMysqlDataSourceInitializer.class)
@Testcontainers
@FieldDefaults(level = AccessLevel.PRIVATE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientIntegrationExceptionTest {

    static MySQLContainer<?> mySQLContainer;

    static {
        mySQLContainer = new MySQLContainer<>("mysql:8.0.26");
        mySQLContainer.start();
    }

    @Autowired
    PatientService patientService;
    @Autowired
    QueueService queueService;
    @Autowired
    TestRestTemplate restTemplate;
    String token;
    Gson gson;

    @LocalServerPort
    int port;

    public String getUrlWithPort(String uri) {
        return "https://localhost:" + port + uri;
    }

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
    void shouldThrowExceptionWhenCreatePatientWithAdmissionDateWeekend() {
        long lastPatientId = patientService.getPatientById((long) patientService.getAllPatients().size()).getId();
        int currentQueuesCount = queueService.getAllCurrentQueues().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Kowalska")
                        .age("4Y")
                        .phoneNumber("123456789")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Katar"))
                        .admissionDate(LocalDate.of(2022, 5, 6))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .referralNr("12134234")
                        .sex("F")
                        .urgent(false).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.INVALID_ADMISSION_DATE, exceptionResponse.getMessage()),
                () -> assertThrows(NotFoundException.class,
                        () -> patientService.getPatientById(lastPatientId + 1)),
                () -> assertEquals(currentQueuesCount, queueService.getAllCurrentQueues().size())
        );
    }

    @Order(1)
    @Test
    void shouldThrowExceptionWhenCreatePatientWithAdmissionDateBeforeTwoWeeksFromNow() {
        long lastPatientId = patientService.getPatientById((long) patientService.getAllPatients().size()).getId();
        int currentQueuesCount = queueService.getAllCurrentQueues().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Kowalska")
                        .age("4Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Katar"))
                        .admissionDate(LocalDate.of(2021, 12, 21))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .referralNr("12134234")
                        .sex("F")
                        .phoneNumber("123456789")
                        .urgent(false).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.INVALID_ADMISSION_DATE, exceptionResponse.getMessage()),
                () -> assertThrows(NotFoundException.class,
                        () -> patientService.getPatientById(lastPatientId + 1)),
                () -> assertEquals(currentQueuesCount, queueService.getAllCurrentQueues().size())
        );
    }

    @Order(3)
    @Test
    void shouldThrowExceptionWhenCreateUrgentPatientNotUrgent() {
        long lastPatientId = patientService.getPatientById((long) patientService.getAllPatients().size()).getId();
        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials("beata.guzik", "password"), null);
        ResponseEntity<String> responseAuth = restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH), HttpMethod.POST,
                credentials, String.class);

        token = responseAuth.getBody();

        int currentQueuesCount = queueService.getAllCurrentQueues().size();

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
                        .phoneNumber("123456789")
                        .sex("F")
                        .urgent(false).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/urgent"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(ErrorKey.PATIENT_NOT_URGENT, exceptionResponse.getMessage()),
                () -> assertThrows(NotFoundException.class,
                        () -> patientService.getPatientById(lastPatientId + 1)),
                () -> assertEquals(currentQueuesCount, queueService.getAllCurrentQueues().size())
        );
    }

    @Order(4)
    @Test
    void shouldThrowExceptionWhenNoPermissionToCreateUrgentPatient() {
        long lastPatientId = patientService.getPatientById((long) patientService.getAllPatients().size()).getId();

        HttpEntity<Credentials> credentials = new HttpEntity<>(
                new Credentials("beata.guzik", "password"), null);
        ResponseEntity<String> responseAuth = restTemplate.exchange(getUrlWithPort(AccountConstants.AUTH), HttpMethod.POST,
                credentials, String.class);

        token = responseAuth.getBody();

        int currentQueuesCount = queueService.getAllCurrentQueues().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Nowak")
                        .age("7Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Y"))
                        .admissionDate(LocalDate.of(2022, 3, 17))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .phoneNumber("123456789")
                        .referralNr("121574234")
                        .sex("F")
                        .urgent(true).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/urgent"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.NO_PERMISSION_TO_CREATE_URGENT_PATIENT, exceptionResponse.getMessage()),
                () -> assertThrows(NotFoundException.class,
                        () -> patientService.getPatientById(lastPatientId + 1)),
                () -> assertEquals(currentQueuesCount, queueService.getAllCurrentQueues().size())
        );
    }

    @Order(5)
    @Test
    void shouldThrowExceptionWhenCreatePatientQueueLocked() {
        long lastPatientId = patientService.getPatientById((long) patientService.getAllPatients().size()).getId();

        int patientsCount = queueService.getQueueForDate(LocalDate.of(2022, 3, 15)).getPatients().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Nowak")
                        .age("7Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Y"))
                        .admissionDate(LocalDate.of(2022, 3, 15))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .referralNr("12159908984")
                        .sex("F")
                        .phoneNumber("123456789")
                        .urgent(false).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.QUEUE_LOCKED_OR_FULL, exceptionResponse.getMessage()),
                () -> assertThrows(NotFoundException.class,
                        () -> patientService.getPatientById(lastPatientId + 1)),
                () -> assertEquals(patientsCount, queueService.getQueueForDate(
                        LocalDate.of(2022, 3, 15)).getPatients().size())
        );
    }

    @Order(6)
    @Test
    void shouldThrowExceptionWhenCreatePatientNoContactInfo() {
        long lastPatientId = patientService.getPatientById((long) patientService.getAllPatients().size()).getId();

        int patientsCount = queueService.getQueueForDate(LocalDate.of(2022, 3, 15)).getPatients().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Nowak")
                        .age("7Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Y"))
                        .admissionDate(LocalDate.of(2022, 3, 15))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .referralNr("12159908984")
                        .sex("F")
                        .urgent(false).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.CONTACT_INFO_REQUIRED, exceptionResponse.getMessage()),
                () -> assertThrows(NotFoundException.class,
                        () -> patientService.getPatientById(lastPatientId + 1)),
                () -> assertEquals(patientsCount, queueService.getQueueForDate(
                        LocalDate.of(2022, 3, 15)).getPatients().size())
        );
    }

    @Order(7)
    @Test
    void shouldThrowExceptionWhenCreatePatientNoReferralInfo() {
        long lastPatientId = patientService.getPatientById((long) patientService.getAllPatients().size()).getId();

        int patientsCount = queueService.getQueueForDate(LocalDate.of(2022, 3, 15)).getPatients().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Nowak")
                        .age("7Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Y"))
                        .admissionDate(LocalDate.of(2022, 3, 15))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .sex("F")
                        .phoneNumber("278478735")
                        .urgent(false).build(), getHttpHeaders()
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients"),
                HttpMethod.POST, patientCreateRequestHttpEntity, String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.REFERRAL_INFO_REQUIRED, exceptionResponse.getMessage()),
                () -> assertThrows(NotFoundException.class,
                        () -> patientService.getPatientById(lastPatientId + 1)),
                () -> assertEquals(patientsCount, queueService.getQueueForDate(
                        LocalDate.of(2022, 3, 15)).getPatients().size())
        );
    }

    @Order(8)
    @Test
    void shouldThrowExceptionWhenConfirmPatientAlreadyConfirmedTwice() {
        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/confirm/1"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.PATIENT_CONFIRMED, exceptionResponse.getMessage())
        );
    }

    @Order(9)
    @Test
    void shouldThrowExceptionWhenConfirmNotUrgentPatientQueueLocked() {
        patientService.changePatientUrgency(9L, false, "sylwester.garda");

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/confirm/9"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        patientService.changePatientUrgency(9L, true, "sylwester.garda");

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.QUEUE_LOCKED, exceptionResponse.getMessage())
        );
    }

    @Order(10)
    @Test
    void shouldThrowExceptionWhenChangeAdmissionDateWeekend() {

        LocalDate oldDate = patientService.getPatientById(11L).getAdmissionDate();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/date/11/07-05-2022"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);


        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.ADMISSION_DATE_WEEKEND, exceptionResponse.getMessage()),
                () -> assertEquals(oldDate, patientService.getPatientById(11L).getAdmissionDate())
        );
    }

    @Order(11)
    @Test
    void shouldThrowExceptionWhenChangeAdmissionDatePatientAdmitted() {

        LocalDate oldDate = patientService.getPatientById(23L).getAdmissionDate();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/date/23/03-05-2022"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);


        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(ErrorKey.PATIENT_ALREADY_ADMITTED, exceptionResponse.getMessage()),
                () -> assertEquals(oldDate, patientService.getPatientById(23L).getAdmissionDate())
        );
    }

    @Order(12)
    @Test
    void shouldThrowExceptionWhenChangeAdmissionDateQueueLocked() {

        LocalDate oldDate = patientService.getPatientById(22L).getAdmissionDate();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/date/22/15-03-2022"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);


        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.QUEUE_LOCKED_OR_FULL, exceptionResponse.getMessage()),
                () -> assertEquals(oldDate, patientService.getPatientById(22L).getAdmissionDate())
        );
    }


    @Order(13)
    @Test
    void shouldThrowExceptionWhenChangeUrgencyPatientAdmitted() {
        boolean oldUrgency = patientService.getPatientById(23L).isUrgent();

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/urgency/23/" + !oldUrgency),
                HttpMethod.GET, getJwtHttpEntity(), String.class);


        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(ErrorKey.PATIENT_ALREADY_ADMITTED, exceptionResponse.getMessage()),
                () -> assertEquals(oldUrgency, patientService.getPatientById(23L).isUrgent())
        );
    }


    @Order(14)
    @Test
    void shouldThrowExceptionWhenDeletePatientConfirmedTwice() {

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/1"),
                HttpMethod.DELETE, getJwtHttpEntity(), String.class);

        ExceptionResponse exceptionResponse = gson.fromJson(response.getBody(), ExceptionResponse.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals(ErrorKey.PATIENT_CONFIRMED, exceptionResponse.getMessage()),
                () -> assertDoesNotThrow(() -> patientService.getPatientById(1L))
        );
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
}
