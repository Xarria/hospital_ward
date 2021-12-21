package com.backend.hospitalward.unit.patient;

import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.*;
import com.backend.hospitalward.model.common.AccessLevelName;
import com.backend.hospitalward.model.common.PatientStatusName;
import com.backend.hospitalward.repository.*;
import com.backend.hospitalward.service.PatientService;
import com.backend.hospitalward.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class PatientUnitTest {

    @Mock
    PatientRepository patientRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    DiseaseRepository diseaseRepository;
    @Mock
    CovidStatusRepository covidStatusRepository;
    @Mock
    PatientTypeRepository patientTypeRepository;
    @Mock
    PatientStatusRepository patientStatusRepository;
    @Mock
    QueueService queueService;

    @InjectMocks
    PatientService patientService;

    Patient patientUrgent;
    Patient patientUrgent2;
    Patient patientOther;
    MedicalStaff doctor;
    List<Patient> allPatients;
    CovidStatus csVaccinated = CovidStatus.builder().status("VACCINATED").build();
    PatientStatus psWaiting = PatientStatus.builder().name(PatientStatusName.WAITING.name()).build();
    PatientStatus psConfirmedTwice = PatientStatus.builder().name(PatientStatusName.CONFIRMED_TWICE.name()).build();
    PatientStatus psConfirmedOnce = PatientStatus.builder().name(PatientStatusName.CONFIRMED_ONCE.name()).build();
    PatientType pt = new PatientType();
    AccessLevel accessLevel = AccessLevel.builder().name(AccessLevelName.DOCTOR).build();
    AccessLevel accessLevelOffice = AccessLevel.builder().name(AccessLevelName.SECRETARY).build();


    @BeforeEach
    void setUp() {
        initFields();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllPatients() {
        when(patientRepository.findAll()).thenReturn(allPatients);

        assertDoesNotThrow(() -> patientService.getAllPatients());

        verify(patientRepository).findAll();
    }

    @Test
    void getPatientById() {
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientUrgent));

        Patient patient = patientService.getPatientById(1L);

        assertEquals(patientUrgent, patient);
    }

    @Test
    void getPatientByIdNotFound() {
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patientService.getPatientById(1L));
    }

    @Test
    void createPatient() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.of(doctor));
        when(accountRepository.findAccountByLogin("createdBy")).thenReturn(Optional.of(Account.builder()
                .accessLevel(accessLevel).build()));
        when(diseaseRepository.findDiseaseByName(any())).thenReturn(Optional.of(new Disease()));
        when(covidStatusRepository.findCovidStatusByStatus(any())).thenReturn(Optional.of(csVaccinated));
        when(patientTypeRepository.findPatientTypeByName(any())).thenReturn(Optional.of(pt));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(new PatientStatus()));
        when(queueService.checkIfPatientCanBeAddedForDate(patientOther.getAdmissionDate())).thenReturn(true);

        patientService.createPatient(patientOther, "createdBy", new ArrayList<>(), "mainDoctor",
                "VACCINATED");

        verify(queueService).addPatientToQueue(patientOther);

    }

    @Test
    void createPatientUrgent() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.of(doctor));
        when(accountRepository.findAccountByLogin("createdBy")).thenReturn(Optional.of(Account.builder()
                .accessLevel(accessLevel).build()));
        when(diseaseRepository.findDiseaseByName(any())).thenReturn(Optional.of(new Disease()));
        when(covidStatusRepository.findCovidStatusByStatus(any())).thenReturn(Optional.of(csVaccinated));
        when(patientTypeRepository.findPatientTypeByName(any())).thenReturn(Optional.of(pt));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(new PatientStatus()));
        when(queueService.checkIfPatientCanBeAddedForDate(patientOther.getAdmissionDate())).thenReturn(false);

        patientService.createPatient(patientUrgent, "createdBy", new ArrayList<>(), "mainDoctor",
                "VACCINATED");

        verify(queueService).addPatientToQueue(patientOther);

    }

    @Test
    void shouldThrowExceptionWhenCreatePatientCannotBeAddedToQueue() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.of(doctor));
        when(accountRepository.findAccountByLogin("createdBy")).thenReturn(Optional.of(Account.builder()
                .accessLevel(accessLevel).build()));
        when(diseaseRepository.findDiseaseByName(any())).thenReturn(Optional.of(new Disease()));
        when(covidStatusRepository.findCovidStatusByStatus(any())).thenReturn(Optional.of(csVaccinated));
        when(patientTypeRepository.findPatientTypeByName(any())).thenReturn(Optional.of(pt));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(new PatientStatus()));
        when(queueService.checkIfPatientCanBeAddedForDate(patientOther.getAdmissionDate())).thenReturn(false);

        assertThrows(ConflictException.class, () -> patientService.createPatient(patientOther, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));
    }

    @Test
    void shouldThrowExceptionWhenCreatePatientNoContactInfo() {
        patientOther.setPhoneNumber(null);

        assertThrows(ConflictException.class, () -> patientService.createPatient(patientOther, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));
    }

    @Test
    void shouldThrowExceptionWhenCreatePatientNoReferralInfo() {
        patientOther.setReferralNr(null);

        assertThrows(ConflictException.class, () -> patientService.createPatient(patientOther, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));
    }

    @Test
    void shouldThrowExceptionWhenCreatePatientCovidStatusNotFound() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.of(doctor));
        when(accountRepository.findAccountByLogin("createdBy")).thenReturn(Optional.of(Account.builder()
                .accessLevel(accessLevel).build()));
        when(diseaseRepository.findDiseaseByName(any())).thenReturn(Optional.of(new Disease()));
        when(covidStatusRepository.findCovidStatusByStatus(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patientService.createPatient(patientOther, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));
    }

    @Test
    void shouldThrowExceptionWhenCreatePatientStatusNotFound() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.of(doctor));
        when(accountRepository.findAccountByLogin("createdBy")).thenReturn(Optional.of(Account.builder()
                .accessLevel(accessLevel).build()));
        when(diseaseRepository.findDiseaseByName(any())).thenReturn(Optional.of(new Disease()));
        when(covidStatusRepository.findCovidStatusByStatus(any())).thenReturn(Optional.of(csVaccinated));
        when(patientTypeRepository.findPatientTypeByName(any())).thenReturn(Optional.of(pt));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patientService.createPatient(patientOther, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));
    }

    @Test
    void shouldThrowExceptionWhenCreatePatientTypeNotFound() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.of(doctor));
        when(accountRepository.findAccountByLogin("createdBy")).thenReturn(Optional.of(Account.builder()
                .accessLevel(accessLevel).build()));
        when(diseaseRepository.findDiseaseByName(any())).thenReturn(Optional.of(new Disease()));
        when(covidStatusRepository.findCovidStatusByStatus(any())).thenReturn(Optional.of(csVaccinated));
        when(patientTypeRepository.findPatientTypeByName(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patientService.createPatient(patientOther, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));
    }

    @Test
    void shouldThrowExceptionWhenCreatePatientUrgentWithoutPermission() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.of(doctor));
        when(accountRepository.findAccountByLogin("createdBy")).thenReturn(Optional.of(Account.builder()
                .accessLevel(accessLevelOffice).build()));

        assertThrows(ConflictException.class, () -> patientService.createPatient(patientUrgent, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));

    }

    @Test
    void shouldThrowExceptionWhenCreatePatientUrgentByThemself() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.of(doctor));

        assertThrows(ConflictException.class, () -> patientService.createPatient(patientUrgent, null,
                new ArrayList<>(), "mainDoctor", "VACCINATED"));

    }

    @Test
    void shouldThrowExceptionWhenCreatePatientMainDoctorNotFound() {
        when(accountRepository.findAccountByLogin("mainDoctor")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patientService.createPatient(patientUrgent, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));

    }

    @Test
    void shouldThrowExceptionWhenCreatePatientAdmissionDateWeekend() {
        patientOther.setAdmissionDate(LocalDate.of(2022, 2, 19));

        assertThrows(ConflictException.class, () -> patientService.createPatient(patientOther, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));

    }

    @Test
    void shouldThrowExceptionWhenCreatePatientAdmissionDateUnderTwoWeeksFromNow() {
        patientOther.setAdmissionDate(LocalDate.of(2021, 12, 15));

        assertThrows(ConflictException.class, () -> patientService.createPatient(patientOther, "createdBy",
                new ArrayList<>(), "mainDoctor", "VACCINATED"));

    }

    @Test
    void updatePatient() {
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));
        when(covidStatusRepository.findCovidStatusByStatus(any())).thenReturn(Optional.of(csVaccinated));
        when(accountRepository.findAccountByLogin("requestedBy")).thenReturn(Optional.of(Account.builder().build()));
        when(diseaseRepository.findDiseaseByName(any())).thenReturn(Optional.of(Disease.builder().build()));
        when(patientTypeRepository.findPatientTypeByName(any())).thenReturn(Optional.of(new PatientType()));

        patientService.updatePatient(1L, Patient.builder().sex("M").urgent(true).build(), List.of("disease"), null,
                "VACCINATED", "requestedBy");

        verify(patientRepository).save(patientOther);
        verify(queueService).refreshQueue(patientOther.getQueue());

        assertEquals("M", patientOther.getSex());
        assertFalse(patientOther.isUrgent());
        assertEquals(csVaccinated, patientOther.getCovidStatus());
    }

    @Test
    void shouldNotUpdatePatientSexWhenValueInvalid() {
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));
        when(covidStatusRepository.findCovidStatusByStatus(any())).thenReturn(Optional.of(csVaccinated));
        when(accountRepository.findAccountByLogin("requestedBy")).thenReturn(Optional.of(Account.builder().build()));

        patientService.updatePatient(1L, Patient.builder().sex("INVALID").urgent(true).build(), null, null,
                "VACCINATED", "requestedBy");

        verify(patientRepository).save(patientOther);

        assertEquals("F", patientOther.getSex());
        assertFalse(patientOther.isUrgent());
        assertEquals(csVaccinated, patientOther.getCovidStatus());
    }

    @Test
    void shouldThrowExceptionWhenUpdatePatientNotFound() {
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> patientService.updatePatient(1L, Patient.builder().sex("INVALID")
                        .urgent(true).build(), null, null, "VACCINATED",
                "requestedBy"));
    }

    @Test
    void confirmPatientStatusChangeToConfirmedTwice() {
        patientOther.setStatus(psConfirmedOnce);
        patientOther.setQueue(Queue.builder().date(LocalDate.of(2022, 3, 7)).build());

        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));
        when(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_TWICE.name()))
                .thenReturn(Optional.of(psConfirmedTwice));
        when(queueService.checkIfQueueForDateIsLocked(any())).thenReturn(false);

        patientService.confirmPatient(1L);

        verify(patientRepository).save(patientOther);

        assertEquals(LocalDate.of(2022, 3, 7), patientOther.getAdmissionDate());
    }

    @Test
    void confirmPatientStatusChangeToConfirmedOnce() {
        patientOther.setStatus(psWaiting);
        patientOther.setQueue(Queue.builder().date(LocalDate.of(2021, 3, 7)).build());

        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));
        when(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_ONCE.name()))
                .thenReturn(Optional.of(psConfirmedOnce));

        patientService.confirmPatient(1L);

        verify(patientRepository).save(patientOther);

        assertEquals(psConfirmedOnce, patientOther.getStatus());
    }

    @Test
    void confirmPatientUrgentQueueLocked() {
        patientUrgent.setStatus(psConfirmedOnce);
        patientUrgent.setQueue(Queue.builder().date(LocalDate.of(2022, 3, 7)).build());

        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientUrgent));
        when(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_TWICE.name()))
                .thenReturn(Optional.of(psConfirmedTwice));
        when(queueService.checkIfQueueForDateIsLocked(any())).thenReturn(true);

        patientService.confirmPatient(1L);

        verify(queueService).switchPatients(any(), any());
        verify(patientRepository).save(patientUrgent);

        assertEquals(LocalDate.of(2022, 3, 7), patientUrgent.getAdmissionDate());
    }

    @Test
    void shouldThrowExceptionWhenConfirmPatientQueueLocked() {
        patientOther.setStatus(psConfirmedOnce);
        patientOther.setQueue(Queue.builder().date(LocalDate.of(2022, 3, 7)).build());

        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));
        when(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_TWICE.name()))
                .thenReturn(Optional.of(psConfirmedTwice));
        when(queueService.checkIfQueueForDateIsLocked(any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> patientService.confirmPatient(1L));

    }

    @Test
    void shouldThrowExceptionWhenConfirmPatientAlreadyConfirmed() {
        patientOther.setStatus(psConfirmedTwice);

        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));

        assertThrows(ConflictException.class, () -> patientService.confirmPatient(1L));

    }

    @Test
    void shouldThrowExceptionWhenChangePatientAdmissionDateWeekend() {
        assertThrows(ConflictException.class, () -> patientService.changePatientAdmissionDate(1L,
                LocalDate.of(2022, 3, 19), "modifiedBy"));

    }

    @Test
    void shouldThrowExceptionWhenChangePatientAdmissionDateAlreadyAdmitted() {
        patientOther.setStatus(psConfirmedTwice);
        patientOther.setAdmissionDate(LocalDate.now().minusDays(10));
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(doctor));
        when(queueService.checkIfPatientCanBeAddedForDate(any())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> patientService.changePatientAdmissionDate(1L,
                LocalDate.of(2022, 2, 16), "modifiedBy"));

        assertEquals(LocalDate.now().minusDays(10), patientOther.getAdmissionDate());
    }

    @Test
    void shouldThrowExceptionWhenChangePatientAdmissionDateCannotBeAddedToQueue() {
        patientOther.setStatus(psConfirmedOnce);
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(doctor));
        when(queueService.checkIfPatientCanBeAddedForDate(any())).thenReturn(false);

        assertThrows(ConflictException.class, () -> patientService.changePatientAdmissionDate(1L,
                LocalDate.of(2022, 2, 16), "modifiedBy"));

        assertEquals(psConfirmedOnce, patientOther.getStatus());
        assertNotEquals(LocalDate.of(2022, 2, 16), patientOther.getAdmissionDate());
    }

    @Test
    void changePatientAdmissionDateUrgentCannotBeAddedToQueue() {
        patientUrgent2.setStatus(psConfirmedOnce);
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientUrgent2));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(psWaiting));
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(doctor));
        when(queueService.checkIfPatientCanBeAddedForDate(any())).thenReturn(false);

        patientService.changePatientAdmissionDate(1L, LocalDate.of(2022, 2, 16),
                "modifiedBy");

        verify(patientRepository).save(patientUrgent2);

        assertEquals(psWaiting, patientUrgent2.getStatus());
        assertEquals(LocalDate.of(2022, 2, 16), patientUrgent2.getAdmissionDate());
    }

    @Test
    void changePatientAdmissionDate() {
        patientUrgent2.setStatus(psConfirmedOnce);
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientUrgent2));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(psWaiting));
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(doctor));
        when(queueService.checkIfPatientCanBeAddedForDate(any())).thenReturn(true);

        patientService.changePatientAdmissionDate(1L, LocalDate.of(2022, 2, 16),
                "modifiedBy");

        verify(patientRepository).save(patientUrgent2);

        assertEquals(psWaiting, patientUrgent2.getStatus());
        assertEquals(LocalDate.of(2022, 2, 16), patientUrgent2.getAdmissionDate());
    }

    @Test
    void changePatientUrgency() {
        patientOther.setStatus(psWaiting);
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));
        when(accountRepository.findAccountByLogin(any())).thenReturn(Optional.of(doctor));

        patientService.changePatientUrgency(1L, true, "modifiedBy");

        verify(patientRepository).save(patientOther);
        verify(queueService).refreshQueue(any());

        assertTrue(patientOther.isUrgent());
    }

    @Test
    void shouldThrowExceptionWhenChangePatientUrgencyAlreadyAdmitted() {
        patientOther.setStatus(psConfirmedTwice);
        patientOther.setAdmissionDate(LocalDate.of(2021, 12, 1));
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientOther));

        assertThrows(BadRequestException.class, () -> patientService.changePatientUrgency(1L, true,
                "modifiedBy"));

        assertFalse(patientOther.isUrgent());
    }

    @Test
    void deletePatient() {
        patientUrgent2.setStatus(psConfirmedOnce);
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientUrgent2));

        patientService.deletePatient(1L);

        verify(queueService).removePatientFromQueue(patientUrgent2);
        verify(patientRepository).delete(patientUrgent2);
    }

    @Test
    void shouldThrowExceptionWhenDeletePatientAlreadyConfirmedTwice() {
        patientUrgent2.setStatus(psConfirmedTwice);
        when(patientRepository.findPatientById(anyLong())).thenReturn(Optional.of(patientUrgent2));

        assertThrows(ConflictException.class, () -> patientService.deletePatient(1L));

        verify(queueService, never()).removePatientFromQueue(patientUrgent2);
        verify(patientRepository, never()).delete(patientUrgent2);
    }

    private void initFields() {
        patientUrgent = Patient.builder()
                .admissionDate(LocalDate.of(2022, 6, 13))
                .urgent(true)
                .phoneNumber("123356889")
                .referralNr("12159908984")
                .age("7M").build();

        patientUrgent2 = Patient.builder()
                .admissionDate(LocalDate.of(2022, 3, 13))
                .urgent(true)
                .phoneNumber("123456889")
                .referralNr("12159908984")
                .age("4Y").build();

        patientOther = Patient.builder()
                .admissionDate(LocalDate.of(2022, 3, 8))
                .urgent(false)
                .age("12Y")
                .phoneNumber("123457789")
                .referralNr("12159908984")
                .sex("F")
                .build();
        allPatients = List.of(patientOther, patientUrgent2, patientUrgent);

        doctor = MedicalStaff.builder().accessLevel(accessLevel).build();
    }

}
