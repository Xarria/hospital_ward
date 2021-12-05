package com.backend.hospitalward.unit.queue;

import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.Disease;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.PatientStatus;
import com.backend.hospitalward.model.Queue;
import com.backend.hospitalward.model.common.PatientStatusName;
import com.backend.hospitalward.repository.PatientRepository;
import com.backend.hospitalward.repository.PatientStatusRepository;
import com.backend.hospitalward.repository.QueueRepository;
import com.backend.hospitalward.service.QueueService;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
class QueueUnitTest {

    @Mock
    QueueRepository queueRepository;

    @Mock
    PatientRepository patientRepository;

    @Mock
    PatientStatusRepository patientStatusRepository;

    @InjectMocks
    QueueService queueService;

    Patient pUrgent;
    Patient pUrgent2;
    Patient pSurgery;
    Patient pSurgery2;
    Patient pSurgery3;
    Patient pOther;
    Patient pOther2;
    Patient pOther3;
    Patient pOther4;
    final Disease diseaseSurgery = Disease.builder().surgeryRequired(true).build();
    final Disease diseaseNoSurgery = Disease.builder().surgeryRequired(false).build();
    PatientStatus psWaiting = PatientStatus.builder().name(PatientStatusName.WAITING.name()).build();
    PatientStatus psConfirmedOnce = PatientStatus.builder().name(PatientStatusName.CONFIRMED_ONCE.name()).build();
    PatientStatus psConfirmedTwice = PatientStatus.builder().name(PatientStatusName.CONFIRMED_TWICE.name()).build();
    Queue emptyCurrentQueue;
    Queue emptyOldQueue;
    Queue fullCurrentQueue;
    Queue fullLockedCurrentQueue;
    Queue notFullCurrentQueue;
    Queue fullUnlockedOldQueue;
    Queue fullUnlockedCurrentQueueWithWaiting;
    Queue fullUnlockedAllConfirmedQueue;

    ArgumentCaptor<Queue> queueCapture = ArgumentCaptor.forClass(Queue.class);

    @BeforeEach
    void setUp() {
        initPatients();
        initQueues();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCurrentQueues() {
        when(queueRepository.findQueuesByDateAfter(any())).thenReturn(List.of(emptyCurrentQueue));

        List<Queue> currentQueues = queueService.getAllCurrentQueues();

        assertEquals(List.of(emptyCurrentQueue), currentQueues);
    }

    @Test
    void getQueueForDate() {
        when(queueRepository.findQueueByDate(Date.valueOf(LocalDate.now()))).thenReturn(Optional.of(emptyCurrentQueue));

        Queue queue = queueService.getQueueForDate(Date.valueOf(LocalDate.now()));

        assertEquals(emptyCurrentQueue, queue);
    }

    @Test
    void findFullAdmissionDates() {
        when(queueRepository.findAll()).thenReturn(List.of(emptyCurrentQueue, emptyOldQueue, fullCurrentQueue,
                fullLockedCurrentQueue, notFullCurrentQueue, fullUnlockedOldQueue));

        List<Queue> fullQueues = List.of(fullCurrentQueue, fullLockedCurrentQueue, fullUnlockedOldQueue);
        List<LocalDate> fullQueuesDates = fullQueues.stream().filter(queue -> queue.getAllPatients().size() >= 8)
                .map(queue -> queue.getDate().toLocalDate()).collect(Collectors.toList());

        List<LocalDate> fullAdmissionDates = queueService.findFullAdmissionDates();

        assertEquals(fullQueuesDates, fullAdmissionDates);
    }

    @Test
    void createQueueForDateIfNotExists() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));

        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        queueService.createQueueForDateIfNotExists(date);

        verify(queueRepository).save(queueCapture.capture());

        assertEquals(date, queueCapture.getValue().getDate());
        assertEquals(new ArrayList<>(), queueCapture.getValue().getPatientsWaiting());
        assertEquals(new ArrayList<>(), queueCapture.getValue().getPatientsConfirmed());
    }

    @Test
    void shouldNotQueueForDateIfNotExistsWhenQueueExists() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));

        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(new Queue()));

        queueService.createQueueForDateIfNotExists(date);

        verify(queueRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDateIsWeekend() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 5));

        assertThrows(ConflictException.class, () -> queueService.createQueueForDateIfNotExists(date));
    }

    @Test
    void checkIfPatientCanBeAddedForDateNoQueue() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertTrue(queueService.checkIfPatientCanBeAddedForDate(date));

        verify(queueRepository).save(any());
    }

    @Test
    void checkIfPatientCanBeAddedForDateQueueUnlockedAndNotFull() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(notFullCurrentQueue));

        assertTrue(queueService.checkIfPatientCanBeAddedForDate(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfPatientCanBeAddedForDateQueueLocked() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullLockedCurrentQueue));

        assertFalse(queueService.checkIfPatientCanBeAddedForDate(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfPatientCanBeAddedForDateQueueFull() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullCurrentQueue));

        assertFalse(queueService.checkIfPatientCanBeAddedForDate(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfQueueForDateIsLockedFullUnlockedQueue() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullCurrentQueue));

        assertFalse(queueService.checkIfQueueForDateIsLocked(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfQueueForDateIsLockedNoQueue() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertFalse(queueService.checkIfQueueForDateIsLocked(date));

        verify(queueRepository).save(any());
    }

    @Test
    void checkIfQueueForDateIsLockedLockedQueue() {
        Date date = Date.valueOf(LocalDate.of(2021, 12, 9));
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullLockedCurrentQueue));

        assertTrue(queueService.checkIfQueueForDateIsLocked(date));

        verify(queueRepository, never()).save(any());
    }

    //public void addPatientToQueue(Patient patient) {
    //        createQueueForDateIfNotExists(patient.getAdmissionDate());
    //
    //        Queue queue = queueRepository.findQueueByDate(patient.getAdmissionDate()).orElseThrow(()
    //                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
    //
    //        queue.getPatientsWaiting().add(patient);
    //
    //        refreshQueue(queue);
    //
    //        queueRepository.save(queue);
    //    }
    @Test
    void addPatientToQueue() {
    }
//public void switchPatientQueue(Patient patient, Date newQueueDate) {
//        Queue oldQueue = queueRepository.findQueueByPatientsWaitingContains(patient).orElseThrow(()
//                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
//        Queue newQueue = queueRepository.findQueueByDate(newQueueDate).orElseThrow(()
//                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
//
//        oldQueue.getPatientsWaiting().remove(patient);
//        oldQueue.getPatientsConfirmed().remove(patient);
//
//        newQueue.getPatientsWaiting().add(patient);
//
//        refreshQueue(oldQueue);
//        refreshQueue(newQueue);
//
//        queueRepository.save(oldQueue);
//        queueRepository.save(newQueue);
//    }
    @Test
    void switchPatientQueue() {
    }
//public void switchPatients(Patient urgentPatient, Date date) {
//        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
//                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
//
//        Patient lowestPriorityPatient = queue.getPatientsConfirmed().stream()
//                .filter(patient -> patient.getPositionInQueue() == queue.getPatientsConfirmed().size() - 1)
//                .findFirst()
//                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));
//
//        queue.getPatientsConfirmed().remove(lowestPriorityPatient);
//        queue.getPatientsConfirmed().add(urgentPatient);
//
//        urgentPatient.setPositionInQueue(lowestPriorityPatient.getPositionInQueue());
//        urgentPatient.setQueue(queue);
//        urgentPatient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_TWICE.name())
//                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));
//
//        lowestPriorityPatient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.WAITING.name())
//                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));
//
//        transferPatientToNextUnlockedQueue(lowestPriorityPatient, date);
//
//        patientRepository.save(lowestPriorityPatient);
//
//        refreshQueue(queue);
//
//        queueRepository.save(queue);
//    }
    @Test
    void switchPatients() {
    }

    //public void transferPatientToNextUnlockedQueue(Patient patient, Date previousDate) {
    //        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalse();
    //        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrue();
    //        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
    //                .map(queue -> queue.getDate().toLocalDate())
    //                .collect(Collectors.toList());
    //
    //        if (!unlockedQueues.isEmpty()) {
    //            unlockedQueues.sort(Comparator.comparing(Queue::getDate));
    //            patient.setQueue(unlockedQueues.get(0));
    //            unlockedQueues.get(0).getPatientsWaiting().add(patient);
    //            refreshQueue(unlockedQueues.get(0));
    //            queueRepository.save(unlockedQueues.get(0));
    //            return;
    //        }
    //
    //        LocalDate date = previousDate.toLocalDate().plusDays(1);
    //
    //        while (true) {
    //            if (checkIfDateIsSaturday(date)) {
    //                date = date.plusDays(2);
    //            } else if (checkIfDateIsSunday(date)) {
    //                date = date.plusDays(1);
    //            }
    //
    //            if (!lockedQueuesDates.contains(date)) {
    //                createQueueForDateIfNotExists(Date.valueOf(date));
    //                Queue createdQueue = queueRepository.findQueueByDate(Date.valueOf(date)).orElseThrow(()
    //                        -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
    //                patient.setQueue(createdQueue);
    //                createdQueue.getPatientsWaiting().add(patient);
    //                patientRepository.save(patient);
    //                queueRepository.save(createdQueue);
    //                break;
    //            } else {
    //                date = date.plusDays(1);
    //            }
    //        }
    //        //TODO email
    //    }
    @Test
    void transferPatientToNextUnlockedQueue() {
    }

    //public void transferPatientsForNextUnlockedDateAndClearOldQueues(Date previousDate, List<Patient> patients) {
    //        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalse();
    //        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrue();
    //        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
    //                .map(queue -> queue.getDate().toLocalDate())
    //                .collect(Collectors.toList());
    //
    //        unlockedQueues.sort(Comparator.comparing(Queue::getDate));
    //
    //        Queue newQueue = findNewQueue(previousDate, unlockedQueues, lockedQueuesDates);
    //
    //        List<Queue> oldQueues = patients.stream().map(Patient::getQueue).distinct().collect(Collectors.toList());
    //
    //        patients.forEach(patient -> patient.setQueue(newQueue));
    //        patients.forEach(patient -> patient.setStatus(patientStatusRepository
    //                .findPatientStatusByName(PatientStatusName.WAITING.name())
    //                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND))));
    //
    //        oldQueues.forEach(queue -> queue.setPatientsWaiting(new ArrayList<>()));
    //        oldQueues.forEach(queue -> queue.setLocked(true));
    //
    //        patients.forEach(patientRepository::save);
    //        oldQueues.forEach(queueRepository::save);
    //
    //    }
    @Test
    void transferPatientsForNextUnlockedDateAndClearOldQueues() {
    }

    //public void lockQueueForDateIfNecessary(Date date) {
    //        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
    //                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
    //        if (queue.getPatientsConfirmed().size() >= 8) {
    //            queue.setLocked(true);
    //            transferPatientsFromLockedQueue(queue);
    //            queueRepository.save(queue);
    //        }
    //    }
    @Test
    void lockQueueForDateIfNecessary() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullUnlockedAllConfirmedQueue));

        queueService.lockQueueForDateIfNecessary(Date.valueOf(LocalDate.now()));

        verify(queueRepository).save(queueCapture.capture());

        assertTrue(queueCapture.getValue().isLocked());
    }

    //List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalse();
    //        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrue();
    //        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
    //                .map(queue -> queue.getDate().toLocalDate())
    //                .collect(Collectors.toList());
    //
    //        unlockedQueues.sort(Comparator.comparing(Queue::getDate));
    //
    //        Queue newQueue = findNewQueue(lockedQueue.getDate(), unlockedQueues, lockedQueuesDates);
    //
    //        lockedQueue.getPatientsWaiting().forEach(patient -> patient.setQueue(newQueue));
    //        lockedQueue.getPatientsWaiting().forEach(patient -> patient.setStatus(patientStatusRepository
    //                .findPatientStatusByName(PatientStatusName.WAITING.name())
    //                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND))));
    //
    //        lockedQueue.setPatientsWaiting(new ArrayList<>());
    //
    //        lockedQueue.getPatientsWaiting().forEach(patientRepository::save);
    //
    //        refreshQueue(newQueue);
    //        queueRepository.save(newQueue);
    @Test
    void lockQueueForDateIfNecessaryNotEmptyWaitingPatientsQueue() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullUnlockedCurrentQueueWithWaiting));

        queueService.lockQueueForDateIfNecessary(Date.valueOf(LocalDate.now()));

        verify(queueRepository).save(queueCapture.capture());

        assertFalse(queueCapture.getValue().isLocked());
    }

    @Test
    void shouldThrowExceptionWhenLockQueueForDateIfNecessaryQueueNotFound() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> queueService.lockQueueForDateIfNecessary(
                Date.valueOf(LocalDate.now())));

    }

    @Test
    void confirmPatient() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(notFullCurrentQueue));
        when(patientRepository.findPatientById(1L)).thenReturn(Optional.of(pUrgent));

        queueService.confirmPatient(pSurgery2, LocalDate.now());

        verify(patientRepository).save(any());
        verify(queueRepository).save(any());

        assertEquals(notFullCurrentQueue.getPatientsWaiting().get(0), pUrgent);
        assertTrue(notFullCurrentQueue.getPatientsConfirmed().contains(pSurgery2));
        assertFalse(notFullCurrentQueue.getPatientsWaiting().contains(pSurgery2));
        assertEquals(0, pUrgent.getPositionInQueue());

    }

    @Test
    void shouldThrowExceptionWhenConfirmPatientQueueNotFound() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> queueService.confirmPatient(pSurgery2, LocalDate.now()));

        verify(patientRepository, never()).save(any());
        verify(queueRepository, never()).save(any());

    }

    @Test
    void removePatientFromQueue() {
        //old order: pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery3
        when(patientRepository.findPatientById(1L)).thenReturn(Optional.of(pUrgent));
        when(patientRepository.findPatientById(2L)).thenReturn(Optional.of(pUrgent2));
        when(patientRepository.findPatientById(4L)).thenReturn(Optional.of(pSurgery2));
        when(patientRepository.findPatientById(8L)).thenReturn(Optional.of(pOther3));
        when(patientRepository.findPatientById(9L)).thenReturn(Optional.of(pOther4));

        when(queueRepository.findQueueByPatientsWaitingContains(pSurgery3)).thenReturn(Optional.of(fullCurrentQueue));

        queueService.removePatientFromQueue(pSurgery3);

        verify(patientRepository, times(5)).save(any());
        verify(queueRepository).save(any());
        //new order: pUrgent2, pUrgent, pSurgery2, pOther3, pOther4
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(0), pUrgent2);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(1), pUrgent);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(2), pSurgery2);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(3), pOther3);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(4), pOther4);
        assertEquals(0, pUrgent2.getPositionInQueue());
        assertEquals(1, pUrgent.getPositionInQueue());
        assertEquals(2, pSurgery2.getPositionInQueue());
        assertEquals(3, pOther3.getPositionInQueue());
        assertEquals(4, pOther4.getPositionInQueue());
    }

    @Test
    void shouldThrowExceptionWhenRemovePatientFromQueueNotFound() {

        when(queueRepository.findQueueByPatientsWaitingContains(pSurgery3)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> queueService.removePatientFromQueue(pSurgery3));

        verify(patientRepository, never()).save(any());
        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfPatientIsInAQueueForDate() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullCurrentQueue));

        assertDoesNotThrow(() -> queueService.checkIfPatientIsInAQueueForDate(Date.valueOf(LocalDate.now().plusDays(3)),
                pOther3));
    }

    @Test
    void shouldThrowExceptionWhenCheckIfPatientIsInAQueueForDateNoQueue() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> queueService.checkIfPatientIsInAQueueForDate(
                Date.valueOf(LocalDate.now().plusDays(3)), pOther3));
    }

    @Test
    void shouldThrowExceptionWhenCheckIfPatientIsInAQueuePatientNotInAQueue() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(emptyCurrentQueue));

        assertThrows(BadRequestException.class, () -> queueService.checkIfPatientIsInAQueueForDate(
                Date.valueOf(LocalDate.now().plusDays(3)), pOther3));
    }

    @Test
    void refreshQueue() {
        //old order: pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery3
        when(patientRepository.findPatientById(1L)).thenReturn(Optional.of(pUrgent));
        when(patientRepository.findPatientById(2L)).thenReturn(Optional.of(pUrgent2));
        when(patientRepository.findPatientById(4L)).thenReturn(Optional.of(pSurgery2));
        when(patientRepository.findPatientById(5L)).thenReturn(Optional.of(pSurgery3));
        when(patientRepository.findPatientById(8L)).thenReturn(Optional.of(pOther3));
        when(patientRepository.findPatientById(9L)).thenReturn(Optional.of(pOther4));

        queueService.refreshQueue(fullCurrentQueue);

        verify(patientRepository, times(6)).save(any());
        //new order: pUrgent2, pUrgent, pSurgery3, pSurgery2, pOther3, pOther4
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(0), pUrgent2);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(1), pUrgent);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(2), pSurgery3);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(3), pSurgery2);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(4), pOther3);
        assertEquals(fullCurrentQueue.getPatientsWaiting().get(5), pOther4);
        assertEquals(0, pUrgent2.getPositionInQueue());
        assertEquals(1, pUrgent.getPositionInQueue());
        assertEquals(2, pSurgery3.getPositionInQueue());
        assertEquals(3, pSurgery2.getPositionInQueue());
        assertEquals(4, pOther3.getPositionInQueue());
        assertEquals(5, pOther4.getPositionInQueue());
    }

    @Test
    void getWaitingPatientsForPastQueues() {
        when(queueRepository.findQueuesByDateBefore(any())).thenReturn(List.of(fullUnlockedOldQueue, emptyOldQueue));

        List<Patient> patients = queueService.getWaitingPatientsForPastQueues();

        assertEquals(fullUnlockedOldQueue.getPatientsWaiting(), patients);
    }


    private void initPatients() {
        pUrgent = Patient.builder().id(1).urgent(true).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().plusDays(2))).build();
        pUrgent2 = Patient.builder().id(2).urgent(true).diseases(List.of(diseaseSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().minusDays(1))).build();
        pSurgery = Patient.builder().id(3).urgent(false).diseases(List.of(diseaseSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().plusDays(2))).build();
        pSurgery2 = Patient.builder().id(4).urgent(false).diseases(List.of(diseaseSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().minusDays(1))).build();
        pSurgery3 = Patient.builder().id(5).urgent(false).diseases(List.of(diseaseSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().minusDays(4))).build();
        pOther = Patient.builder().id(6).urgent(false).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().minusDays(4))).build();
        pOther2 = Patient.builder().id(7).urgent(false).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().minusDays(2))).build();
        pOther3 = Patient.builder().id(8).urgent(false).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().minusDays(1))).build();
        pOther4 = Patient.builder().id(9).urgent(false).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(Date.valueOf(LocalDate.now().plusDays(2))).build();
    }

    private void initQueues() {
        emptyCurrentQueue = Queue.builder().date(Date.valueOf(LocalDate.now().plusDays(1))).locked(false)
                .patientsWaiting(Collections.emptyList()).patientsConfirmed(Collections.emptyList()).build();
        emptyOldQueue = Queue.builder().date(Date.valueOf(LocalDate.now().minusDays(6)))
                .patientsWaiting(Collections.emptyList()).patientsConfirmed(Collections.emptyList()).build();
        fullCurrentQueue = Queue.builder().date(Date.valueOf(LocalDate.now().plusDays(3))).locked(false)
                .patientsWaiting(List.of(pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery3))
                .patientsConfirmed(List.of(pOther2, pOther)).build();
        fullLockedCurrentQueue = Queue.builder().date(Date.valueOf(LocalDate.now().plusDays(4))).locked(true)
                .patientsWaiting(Collections.emptyList())
                .patientsConfirmed(List.of(pOther2, pOther, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2))
                .build();
        fullUnlockedAllConfirmedQueue = Queue.builder().date(Date.valueOf(LocalDate.now().plusDays(4))).locked(false)
                .patientsWaiting(Collections.emptyList())
                .patientsConfirmed(List.of(pOther2, pOther, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2))
                .build();
        notFullCurrentQueue = Queue.builder().date(Date.valueOf(LocalDate.now().plusDays(5))).locked(false)
                .patientsWaiting(List.of(pUrgent, pSurgery2))
                .patientsConfirmed(List.of(pOther2, pOther)).build();
        fullUnlockedOldQueue = Queue.builder().date(Date.valueOf(LocalDate.now().minusDays(2))).locked(false)
                .patientsWaiting(List.of(pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery))
                .patientsConfirmed(List.of(pOther2, pOther, pSurgery3)).build();
        fullUnlockedCurrentQueueWithWaiting = Queue.builder().date(Date.valueOf(LocalDate.now().plusDays(4)))
                .locked(false).patientsWaiting(List.of(pSurgery))
                .patientsConfirmed(List.of(pOther2, pOther, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2))
                .build();
    }
}
