package com.backend.hospitalward.integration.patient;

import com.backend.hospitalward.common.AccountConstants;
import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.dto.request.patient.PatientCreateRequest;
import com.backend.hospitalward.dto.request.patient.PatientUpdateRequest;
import com.backend.hospitalward.dto.response.patient.PatientDetailsResponse;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.integration.AbstractTestContainer;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.Queue;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.PatientService;
import com.backend.hospitalward.service.QueueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Objects;

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
    String token;
    ObjectMapper objectMapper;

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
    public void setUpJsonMapper() {
        objectMapper = new ObjectMapper();
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
    void getPatientById() throws JsonProcessingException {
        Patient patient = patientService.getPatientById(20L);
        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/20")
                , HttpMethod.GET, getJwtHttpEntity(), String.class);

        PatientDetailsResponse patientDetailsResponse = objectMapper.readValue(response.getBody(), PatientDetailsResponse.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(patient.getName(), patientDetailsResponse.getName()),
                () -> assertEquals(patient.getDiseases().size(), patientDetailsResponse.getDiseases().size()),
                () -> assertEquals(patient.getDiseases().get(0).getLatinName(),
                        patientDetailsResponse.getDiseases().get(0).getLatinName())
        );
    }

    @Order(2)
    @Test
    void shouldCreateNewQueueWhenCreatePatientWithNonExistingDate() {
        int currentQueuesCount = queueService.getAllCurrentQueues().size();

        HttpEntity<PatientCreateRequest> patientCreateRequestHttpEntity = new HttpEntity<>(
                PatientCreateRequest.builder()
                        .name("Alicja")
                        .surname("Kowalska")
                        .age("4Y")
                        .covidStatus("VACCINATED")
                        .diseases(List.of("Ostium secundum"))
                        .admissionDate(LocalDate.of(2022, 5, 4))
                        .mainDoctor("jan.kowalski")
                        .pesel("45565555555")
                        .referralNr("12134234")
                        .phoneNumber("123456789")
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

    @Order(3)
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
                        .diseases(List.of("Dissociatio atrioventricularis"))
                        .phoneNumber("123456780")
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
                () -> assertEquals(4, newPatient.getPositionInQueue()),
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

    @Order(4)
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
                        .phoneNumber("123456556")
                        .diseases(List.of("Dissociatio atrioventricularis"))
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
                () -> assertEquals(3, newPatient.getPositionInQueue()),
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

    @Order(5)
    @Test
    void shouldNotChangePatientPositionWhenUpdatePatientWithoutDisease() {
        Long version = patientService.getPatientById(20L).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort("/patients/20")
                , HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        Patient patient = patientService.getPatientById(20L);
        int currentPosition = patient.getPositionInQueue();

        assertEquals("BOY", patient.getPatientType().getName());
        assertEquals("Dawid", patient.getName());

        HttpEntity<PatientUpdateRequest> patientUpdateRequestHttpEntity = new HttpEntity<>(
                PatientUpdateRequest.builder()
                        .name("Norbert")
                        .version(version)
                        .age("5Y").build(), headers
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/20"),
                HttpMethod.PUT, patientUpdateRequestHttpEntity, String.class);

        Patient updatedPatient = patientService.getPatientById(20L);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("UNDER_6", updatedPatient.getPatientType().getName()),
                () -> assertEquals("Norbert", updatedPatient.getName()),
                () -> assertEquals("5Y", updatedPatient.getAge()),
                () -> assertEquals(currentPosition, updatedPatient.getPositionInQueue())
        );
    }

    @Order(6)
    @Test
    void shouldChangePatientPositionWhenUpdatePatientDisease() {
        Long version = patientService.getPatientById(22L).getVersion();

        ResponseEntity<String> responseGet = restTemplate.exchange(getUrlWithPort("/patients/22")
                , HttpMethod.GET, getJwtHttpEntity(), String.class);

        String etag = Objects.requireNonNull(responseGet.getHeaders().get(HttpHeaders.ETAG)).get(0);

        HttpHeaders headers = getHttpHeaders();
        headers.add(HttpHeaders.IF_MATCH, etag.substring(1, etag.length() - 1));

        Patient patient = patientService.getPatientById(22L);

        assertEquals("Paulina", patient.getName());
        assertEquals(5, patient.getPositionInQueue());

        HttpEntity<PatientUpdateRequest> patientUpdateRequestHttpEntity = new HttpEntity<>(
                PatientUpdateRequest.builder()
                        .name("Janina")
                        .version(version)
                        .diseases(List.of("Ostium secundum")).build(), headers
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/22"),
                HttpMethod.PUT, patientUpdateRequestHttpEntity, String.class);

        Patient updatedPatient = patientService.getPatientById(22L);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("Ostium secundum", updatedPatient.getDiseases().get(0).getLatinName()),
                () -> assertEquals("Janina", updatedPatient.getName()),
                () -> assertEquals(4, updatedPatient.getPositionInQueue())
        );
    }

    @Order(7)
    @Test
    void shouldChangeStatusToConfirmedOnceAndLeaveQueueUnlockedWhenConfirmPatient() {
        Patient patientBefore = patientService.getPatientById(22L);
        LocalDate admissionDate = patientBefore.getAdmissionDate();
        Queue patientQueue = patientBefore.getQueue();

        assertAll(
                () -> assertEquals("WAITING", patientBefore.getStatus().getName()),
                () -> assertEquals(4, patientBefore.getPositionInQueue())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/confirm/22"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient confirmedPatient = patientService.getPatientById(22L);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("CONFIRMED_ONCE", confirmedPatient.getStatus().getName()),
                () -> assertEquals(4, confirmedPatient.getPositionInQueue()),
                () -> assertEquals(admissionDate, confirmedPatient.getAdmissionDate()),
                () -> assertEquals(patientQueue.getDate(), confirmedPatient.getQueue().getDate()),
                () -> assertFalse(confirmedPatient.getQueue().isLocked()),
                () -> assertTrue(confirmedPatient.getQueue().getWaitingPatients().contains(confirmedPatient))
        );
    }

    @Order(8)
    @Test
    void shouldChangeStatusToConfirmedTwiceAndLeaveQueueUnlockedWhenConfirmPatient() {
        Patient patientBefore = patientService.getPatientById(21L);
        Queue patientQueue = patientBefore.getQueue();

        assertAll(
                () -> assertEquals("CONFIRMED_ONCE", patientBefore.getStatus().getName()),
                () -> assertEquals(3, patientBefore.getPositionInQueue())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/confirm/21"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient confirmedPatient = patientService.getPatientById(21L);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("CONFIRMED_TWICE", confirmedPatient.getStatus().getName()),
                () -> assertEquals(3, confirmedPatient.getPositionInQueue()),
                () -> assertEquals(confirmedPatient.getQueue().getDate(), confirmedPatient.getAdmissionDate()),
                () -> assertEquals(patientQueue.getDate(), confirmedPatient.getQueue().getDate()),
                () -> assertFalse(confirmedPatient.getQueue().isLocked()),
                () -> assertTrue(confirmedPatient.getQueue().getConfirmedPatients().contains(confirmedPatient))
        );
    }

    @Order(9)
    @Test
    void shouldChangeStatusToConfirmedTwiceLockQueueAndTransferWaitingPatientsWhenConfirmPatient() {
        Patient patientBefore = patientService.getPatientById(18L);
        Queue patientQueue = patientBefore.getQueue();

        Queue expectedNewQueue = queueService.getQueueForDate(LocalDate.of(2022, 3, 17));
        int expectedQueueSizeWaiting = expectedNewQueue.getWaitingPatients().size();

        assertAll(
                () -> assertEquals("CONFIRMED_ONCE", patientBefore.getStatus().getName()),
                () -> assertEquals(7, patientBefore.getPositionInQueue()),
                () -> assertEquals(2, patientBefore.getQueue().getWaitingPatients().size())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/confirm/18"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient confirmedPatient = patientService.getPatientById(18L);
        Queue expectedNewQueueAfter = queueService.getQueueForDate(LocalDate.of(2022, 3, 17));

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("CONFIRMED_TWICE", confirmedPatient.getStatus().getName()),
                () -> assertEquals(7, confirmedPatient.getPositionInQueue()),
                () -> assertEquals(confirmedPatient.getQueue().getDate(), confirmedPatient.getAdmissionDate()),
                () -> assertEquals(patientQueue.getDate(), confirmedPatient.getQueue().getDate()),
                () -> assertTrue(confirmedPatient.getQueue().isLocked()),
                () -> assertTrue(confirmedPatient.getQueue().getConfirmedPatients().contains(confirmedPatient)),
                () -> assertTrue(confirmedPatient.getQueue().getWaitingPatients().isEmpty()),
                () -> assertEquals(expectedQueueSizeWaiting + 1, expectedNewQueueAfter.getWaitingPatients().size()),
                () -> assertEquals(10L, expectedNewQueueAfter.getWaitingPatients().get(0).getId()),
                () -> assertEquals(6, patientService.getPatientById(10L).getPositionInQueue()),
                () -> assertEquals("WAITING", patientService.getPatientById(10L).getStatus().getName()),
                () -> assertEquals(expectedNewQueue.getDate(), patientService.getPatientById(10L).getQueue().getDate())
        );
    }

    @Order(10)
    @Test
    void shouldSwitchPatientsWhenQueueIsLockedAndConfirmPatient() {
        Patient patientBefore = patientService.getPatientById(9L);
        Queue patientQueue = queueService.getQueueForDate(patientBefore.getQueue().getDate());
        long lowestPriorityPatientId = patientQueue.getPatients().stream()
                .filter(p -> p.getPositionInQueue() == patientQueue.getPatients().size() - 1).findFirst().get().getId();
        assertAll(
                () -> assertEquals("CONFIRMED_ONCE", patientBefore.getStatus().getName()),
                () -> assertEquals(2, patientBefore.getPositionInQueue()),
                () -> assertEquals(2, patientBefore.getQueue().getWaitingPatients().size()),
                () -> assertEquals("CONFIRMED_TWICE", patientService.getPatientById(lowestPriorityPatientId)
                        .getStatus().getName())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/confirm/9"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient confirmedPatient = patientService.getPatientById(9L);
        Queue expectedNewQueueAfter = queueService.getQueueForDate(LocalDate.of(2022, 3, 17));

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("CONFIRMED_TWICE", confirmedPatient.getStatus().getName()),
                () -> assertEquals(2, confirmedPatient.getPositionInQueue()),
                () -> assertEquals(confirmedPatient.getQueue().getDate(), confirmedPatient.getAdmissionDate()),
                () -> assertEquals(patientQueue.getDate(), confirmedPatient.getQueue().getDate()),
                () -> assertTrue(confirmedPatient.getQueue().isLocked()),
                () -> assertTrue(confirmedPatient.getQueue().getConfirmedPatients().contains(confirmedPatient)),
                () -> assertEquals(0, confirmedPatient.getQueue().getWaitingPatients().size()),
                () -> assertFalse(confirmedPatient.getQueue().getPatients().contains(patientService.getPatientById(lowestPriorityPatientId))),
                () -> assertTrue(expectedNewQueueAfter.getWaitingPatients().contains(patientService.getPatientById(lowestPriorityPatientId))),
                () -> assertEquals(8, patientService.getPatientById(lowestPriorityPatientId).getPositionInQueue())
        );
    }

    @Order(11)
    @Test
    void shouldChangeAdmissionDateToExistingQueueUrgentPatient() {
        Patient patientBefore = patientService.getPatientById(11L);
        assertAll(
                () -> assertEquals(LocalDate.of(2022, 3, 16), patientBefore.getQueue().getDate()),
                () -> assertEquals(0, patientBefore.getPositionInQueue()),
                () -> assertTrue(patientBefore.isUrgent()),
                () -> assertEquals("CONFIRMED_TWICE", patientBefore.getStatus().getName()),
                () -> assertEquals(0, queueService.getQueueForDate(
                        LocalDate.of(2022, 3, 15)).getWaitingPatients().size())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/date/11/15-03-2022"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient newDatePatient = patientService.getPatientById(11L);
        Queue expectedNewQueue = queueService.getQueueForDate(LocalDate.of(2022, 3, 15));

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("WAITING", newDatePatient.getStatus().getName()),
                () -> assertEquals(2, newDatePatient.getPositionInQueue()),
                () -> assertEquals(expectedNewQueue.getDate(), newDatePatient.getAdmissionDate()),
                () -> assertEquals(1, queueService.getQueueForDate(
                        LocalDate.of(2022, 3, 15)).getWaitingPatients().size()),
                () -> assertTrue(expectedNewQueue.getWaitingPatients().contains(newDatePatient))
        );
    }

    @Order(12)
    @Test
    void shouldChangeAdmissionDateToExistingQueueAndUnlockQueue() {
        Patient patientBefore = patientService.getPatientById(1L);
        assertAll(
                () -> assertEquals(LocalDate.of(2022, 3, 15), patientBefore.getQueue().getDate()),
                () -> assertEquals(0, patientBefore.getPositionInQueue()),
                () -> assertEquals(8, patientBefore.getQueue().getConfirmedPatients().size()),
                () -> assertEquals("CONFIRMED_TWICE", patientBefore.getStatus().getName()),
                () -> assertTrue(patientBefore.getQueue().isLocked())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/date/1/17-03-2022"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient newDatePatient = patientService.getPatientById(1L);
        Queue expectedNewQueue = queueService.getQueueForDate(LocalDate.of(2022, 3, 17));

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("WAITING", newDatePatient.getStatus().getName()),
                () -> assertEquals(0, newDatePatient.getPositionInQueue()),
                () -> assertEquals(expectedNewQueue.getDate(), newDatePatient.getAdmissionDate()),
                () -> assertFalse(queueService.getQueueForDate(LocalDate.of(2022, 3, 15)).isLocked()),
                () -> assertEquals(7, queueService.getQueueForDate(
                        LocalDate.of(2022, 3, 15)).getConfirmedPatients().size()),
                () -> assertTrue(expectedNewQueue.getWaitingPatients().contains(newDatePatient))
        );
    }

    @Order(13)
    @Test
    void shouldChangeAdmissionDateToNewQueue() {
        Patient patientBefore = patientService.getPatientById(2L);
        assertAll(
                () -> assertEquals(LocalDate.of(2022, 3, 15), patientBefore.getQueue().getDate()),
                () -> assertEquals(0, patientBefore.getPositionInQueue()),
                () -> assertEquals(7, patientBefore.getQueue().getConfirmedPatients().size()),
                () -> assertEquals("CONFIRMED_TWICE", patientBefore.getStatus().getName())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/date/2/24-03-2022"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient newDatePatient = patientService.getPatientById(2L);
        Queue expectedNewQueue = queueService.getQueueForDate(LocalDate.of(2022, 3, 24));

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("WAITING", newDatePatient.getStatus().getName()),
                () -> assertEquals(0, newDatePatient.getPositionInQueue()),
                () -> assertEquals(expectedNewQueue.getDate(), newDatePatient.getAdmissionDate()),
                () -> assertEquals(6, queueService.getQueueForDate(
                        LocalDate.of(2022, 3, 15)).getConfirmedPatients().size()),
                () -> assertTrue(expectedNewQueue.getWaitingPatients().contains(newDatePatient))
        );
    }

    @Order(14)
    @Test
    void shouldChangeUrgencyToUrgent() {
        Patient patientBefore = patientService.getPatientById(22L);

        assertAll(
                () -> assertFalse(patientBefore.isUrgent()),
                () -> assertEquals(5, patientBefore.getPositionInQueue())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/urgency/22/true"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient urgentPatient = patientService.getPatientById(22L);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertTrue(urgentPatient.isUrgent()),
                () -> assertEquals(0, urgentPatient.getPositionInQueue())
        );
    }

    @Order(15)
    @Test
    void shouldChangeUrgencyToNotUrgent() {
        Patient patientBefore = patientService.getPatientById(1L);

        assertAll(
                () -> assertTrue(patientBefore.isUrgent()),
                () -> assertEquals(2, patientBefore.getPositionInQueue())
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/urgency/1/false"),
                HttpMethod.GET, getJwtHttpEntity(), String.class);

        Patient urgentPatient = patientService.getPatientById(1L);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertFalse(urgentPatient.isUrgent()),
                () -> assertEquals(5, urgentPatient.getPositionInQueue())
        );
    }

    @Order(16)
    @Test
    void shouldDeletePatientWhenNotConfirmed() {
        Patient patient = patientService.getPatientById(22L);
        LocalDate patientQueueDate = patient.getQueue().getDate();

        assertAll(
                () -> assertDoesNotThrow(() -> patientService.getPatientById(22L)),
                () -> assertTrue(queueService.getQueueForDate(patientQueueDate).getPatients().contains(patient))
        );

        ResponseEntity<String> response = restTemplate.exchange(getUrlWithPort("/patients/22"),
                HttpMethod.DELETE, getJwtHttpEntity(), String.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertThrows(NotFoundException.class, () -> patientService.getPatientById(22L)),
                () -> assertFalse(queueService.getQueueForDate(patientQueueDate).getPatients().contains(patient))
        );
    }
}
