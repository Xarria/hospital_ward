package com.backend.hospitalward.integration.patient;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientIntegrationExceptionTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAllPatients() {
    }

    @Test
    void getPatientById() {
    }

    @Test
    void createPatient() {
    }

    @Test
    void createUrgentPatient() {
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
