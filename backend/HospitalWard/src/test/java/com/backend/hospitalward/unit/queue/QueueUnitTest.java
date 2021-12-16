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
    final PatientStatus psWaiting = PatientStatus.builder().name(PatientStatusName.WAITING.name()).build();
    final PatientStatus psConfirmedTwice = PatientStatus.builder().name(PatientStatusName.CONFIRMED_TWICE.name()).build();
    Queue emptyCurrentQueue;
    Queue emptyOldQueue;
    Queue fullCurrentQueue;
    Queue fullLockedCurrentQueue;
    Queue notFullCurrentQueue;
    Queue fullUnlockedOldQueue;
    Queue fullUnlockedCurrentQueueWithWaiting;
    Queue fullUnlockedAllConfirmedQueue;

    final ArgumentCaptor<Queue> queueCapture = ArgumentCaptor.forClass(Queue.class);

    @BeforeEach
    void setUp() {
        initPatients();
        initQueues();
        MockitoAnnotations.openMocks(this);

        when(patientRepository.findPatientById(1L)).thenReturn(Optional.of(pUrgent));
        when(patientRepository.findPatientById(2L)).thenReturn(Optional.of(pUrgent2));
        when(patientRepository.findPatientById(3L)).thenReturn(Optional.of(pSurgery));
        when(patientRepository.findPatientById(4L)).thenReturn(Optional.of(pSurgery2));
        when(patientRepository.findPatientById(5L)).thenReturn(Optional.of(pSurgery3));
        when(patientRepository.findPatientById(6L)).thenReturn(Optional.of(pOther));
        when(patientRepository.findPatientById(7L)).thenReturn(Optional.of(pOther2));
        when(patientRepository.findPatientById(8L)).thenReturn(Optional.of(pOther3));
        when(patientRepository.findPatientById(9L)).thenReturn(Optional.of(pOther4));
    }

    @Test
    void getAllCurrentQueues() {
        when(queueRepository.findQueuesByDateAfter(any())).thenReturn(List.of(emptyCurrentQueue));

        List<Queue> currentQueues = queueService.getAllCurrentQueues();

        assertEquals(List.of(emptyCurrentQueue), currentQueues);
    }

    @Test
    void getQueueForDate() {
        when(queueRepository.findQueueByDate(LocalDate.now())).thenReturn(Optional.of(emptyCurrentQueue));

        Queue queue = queueService.getQueueForDate(LocalDate.now());

        assertEquals(emptyCurrentQueue, queue);
    }

    @Test
    void findFullAdmissionDates() {
        when(queueRepository.findAll()).thenReturn(List.of(emptyCurrentQueue, emptyOldQueue, fullCurrentQueue,
                fullLockedCurrentQueue, notFullCurrentQueue, fullUnlockedOldQueue));

        List<Queue> fullQueues = List.of(fullCurrentQueue, fullLockedCurrentQueue, fullUnlockedOldQueue);
        List<LocalDate> fullQueuesDates = fullQueues.stream().filter(queue -> queue.getPatients().size() >= 8)
                .map(Queue::getDate).collect(Collectors.toList());

        List<LocalDate> fullAdmissionDates = queueService.findFullAdmissionDates();

        assertEquals(fullQueuesDates, fullAdmissionDates);
    }

    @Test
    void createQueueForDateIfNotExists() {
        LocalDate date = LocalDate.of(2021, 12, 9);

        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        queueService.createQueueForDateIfNotExists(date);

        verify(queueRepository).save(queueCapture.capture());

        assertEquals(date, queueCapture.getValue().getDate());
        assertEquals(new ArrayList<>(), queueCapture.getValue().getPatients());
    }

    @Test
    void shouldNotQueueForDateIfNotExistsWhenQueueExists() {
        LocalDate date = LocalDate.of(2021, 12, 9);

        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(new Queue()));

        queueService.createQueueForDateIfNotExists(date);

        verify(queueRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDateIsWeekend() {
        LocalDate date = LocalDate.of(2021, 12, 5);

        assertThrows(ConflictException.class, () -> queueService.createQueueForDateIfNotExists(date));
    }

    @Test
    void checkIfPatientCanBeAddedForDateNoQueue() {
        LocalDate date = LocalDate.of(2021, 12, 9);
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertTrue(queueService.checkIfPatientCanBeAddedForDate(date));

        verify(queueRepository).save(any());
    }

    @Test
    void checkIfPatientCanBeAddedForDateQueueUnlockedAndNotFull() {
        LocalDate date = LocalDate.of(2021, 12, 9);
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(notFullCurrentQueue));

        assertTrue(queueService.checkIfPatientCanBeAddedForDate(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfPatientCanBeAddedForDateQueueLocked() {
        LocalDate date = LocalDate.of(2021, 12, 9);
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullLockedCurrentQueue));

        assertFalse(queueService.checkIfPatientCanBeAddedForDate(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfPatientCanBeAddedForDateQueueFull() {
        LocalDate date = LocalDate.of(2021, 12, 9);
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullCurrentQueue));

        assertFalse(queueService.checkIfPatientCanBeAddedForDate(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfQueueForDateIsLockedFullUnlockedQueue() {
        LocalDate date = LocalDate.of(2021, 12, 9);
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullCurrentQueue));

        assertFalse(queueService.checkIfQueueForDateIsLocked(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfQueueForDateIsLockedNoQueue() {
        LocalDate date = LocalDate.of(2021, 12, 9);
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertFalse(queueService.checkIfQueueForDateIsLocked(date));

        verify(queueRepository).save(any());
    }

    @Test
    void checkIfQueueForDateIsLockedLockedQueue() {
        LocalDate date = LocalDate.of(2021, 12, 9);
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullLockedCurrentQueue));

        assertTrue(queueService.checkIfQueueForDateIsLocked(date));

        verify(queueRepository, never()).save(any());
    }

    @Test
    void addPatientToQueue() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(notFullCurrentQueue));

        notFullCurrentQueue.setDate(LocalDate.of(2021, 12, 6));
        pUrgent2.setAdmissionDate(notFullCurrentQueue.getDate());

        queueService.addPatientToQueue(pUrgent2);

        verify(patientRepository, times(6)).save(any());
        verify(queueRepository).save(notFullCurrentQueue);

        assertEquals(notFullCurrentQueue.getPatients().get(0), pUrgent2);
        assertEquals(notFullCurrentQueue.getPatients().get(1), pUrgent);
        assertEquals(notFullCurrentQueue.getPatients().get(2), pSurgery2);
        assertEquals(0, pUrgent2.getPositionInQueue());
        assertEquals(1, pUrgent.getPositionInQueue());
        assertEquals(2, pSurgery2.getPositionInQueue());
        assertEquals(notFullCurrentQueue, pUrgent2.getQueue());
    }

    @Test
    void switchPatientQueue() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(notFullCurrentQueue));
        when(queueRepository.findQueueByPatientsContains(pUrgent2)).thenReturn(Optional.of(fullCurrentQueue));

        fullCurrentQueue.setPatients(List.of(pUrgent2, pSurgery3, pOther4, pOther3));

        queueService.switchPatientQueue(pUrgent2, notFullCurrentQueue.getDate());

        verify(patientRepository, times(8)).save(any());
        assertEquals(fullCurrentQueue.getPatients().get(0), pSurgery3);
        assertEquals(fullCurrentQueue.getPatients().get(1), pOther3);
        assertEquals(fullCurrentQueue.getPatients().get(2), pOther4);
        assertEquals(0, pSurgery3.getPositionInQueue());
        assertEquals(1, pOther3.getPositionInQueue());
        assertEquals(2, pOther4.getPositionInQueue());
        assertEquals(notFullCurrentQueue.getPatients().get(0), pUrgent2);
        assertEquals(notFullCurrentQueue.getPatients().get(1), pUrgent);
        assertEquals(notFullCurrentQueue.getPatients().get(2), pSurgery2);
        assertEquals(notFullCurrentQueue, pUrgent2.getQueue());
        assertEquals(0, pUrgent2.getPositionInQueue());
        assertEquals(1, pUrgent.getPositionInQueue());
        assertEquals(2, pSurgery2.getPositionInQueue());

    }

    @Test
    void switchPatients() {
        //given
        pOther2.setPositionInQueue(6);
        pOther.setPositionInQueue(5);
        pSurgery3.setPositionInQueue(4);
        pUrgent.setPositionInQueue(3);
        pUrgent2.setPositionInQueue(2);
        pOther3.setPositionInQueue(1);
        pSurgery2.setPositionInQueue(0);
        pOther4.setPositionInQueue(7);
        pOther4.setStatus(psConfirmedTwice);
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullLockedCurrentQueue));
        when(queueRepository.findQueuesByLockedFalseAndDateAfter(any())).thenReturn(List.of(fullCurrentQueue,
                emptyCurrentQueue));
        when(queueRepository.findQueuesByLockedTrueAndDateAfter(any())).thenReturn(List.of(fullLockedCurrentQueue));
        when(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_TWICE.name()))
                .thenReturn(Optional.of(psConfirmedTwice));
        when(patientStatusRepository.findPatientStatusByName(PatientStatusName.WAITING.name()))
                .thenReturn(Optional.of(psWaiting));
        //when

        queueService.switchPatients(pSurgery, LocalDate.of(2021, 12, 6));
        //then

        verify(patientRepository).save(pOther4);
        verify(queueRepository).save(emptyCurrentQueue);
        verify(queueRepository).save(fullLockedCurrentQueue);

        assertTrue(fullLockedCurrentQueue.getConfirmedPatients().contains(pSurgery));
        assertEquals(fullLockedCurrentQueue, pSurgery.getQueue());
        assertEquals(1, emptyCurrentQueue.getWaitingPatients().size());
        assertEquals(emptyCurrentQueue.getWaitingPatients().get(0), pOther4);
        assertEquals(0, pOther4.getPositionInQueue());
        assertEquals(emptyCurrentQueue, pOther4.getQueue());
    }

    @Test
    void transferPatientToNextUnlockedQueue() {
        //given
        when(queueRepository.findQueuesByLockedFalseAndDateAfter(any())).thenReturn(List.of(fullCurrentQueue,
                emptyCurrentQueue));
        when(queueRepository.findQueuesByLockedTrueAndDateAfter(any())).thenReturn(List.of(fullLockedCurrentQueue));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(psWaiting));
        //when

        queueService.transferPatientToNextUnlockedQueue(pOther3, LocalDate.of(2021, 12, 6));
        //then

        verify(patientRepository).save(any());
        verify(queueRepository).save(emptyCurrentQueue);

        assertEquals(1, emptyCurrentQueue.getWaitingPatients().size());
        assertEquals(emptyCurrentQueue.getWaitingPatients().get(0), pOther3);
        assertEquals(0, pOther3.getPositionInQueue());
        assertEquals(emptyCurrentQueue, pOther3.getQueue());
    }

    @Test
    void transferPatientToNextUnlockedQueueNoUnlockedQueue() {
        //given
        Queue newQueue = Queue.builder().build();
        when(queueRepository.findQueuesByLockedFalseAndDateAfter(any())).thenReturn(Collections.emptyList());
        when(queueRepository.findQueuesByLockedTrueAndDateAfter(any())).thenReturn(List.of(fullLockedCurrentQueue));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(psWaiting));
        when(queueRepository.findQueueByDate(LocalDate.of(2021, 12, 8)))
                .thenReturn(Optional.of(newQueue));
        //when

        fullLockedCurrentQueue.setDate(LocalDate.of(2021, 12, 7));

        queueService.transferPatientToNextUnlockedQueue(pOther3, LocalDate.of(2021, 12, 6));
        //then

        verify(patientRepository).save(pOther3);
        verify(queueRepository).save(newQueue);

        assertEquals(1, newQueue.getPatients().size());
        assertEquals(newQueue.getPatients().get(0), pOther3);
        assertEquals(0, pOther3.getPositionInQueue());
        assertEquals(newQueue, pOther3.getQueue());
    }

    @Test
    void transferPatientsForNextUnlockedDateAndClearOldQueuesWhenThereAreUnlockedQueues() {
        //give
        List.of(pOther, pOther2, pSurgery3).forEach(p -> p.setStatus(psConfirmedTwice));

        when(queueRepository.findQueuesByLockedFalseAndDateAfter(any())).thenReturn(List.of(fullCurrentQueue,
                notFullCurrentQueue, emptyCurrentQueue));
        when(queueRepository.findQueuesByLockedTrueAndDateAfter(any())).thenReturn(List.of(fullLockedCurrentQueue));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(psWaiting));

        List<Patient> patients = List.of(pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery);
        patients.forEach(p -> p.setQueue(fullUnlockedOldQueue));
        //when

        queueService.transferPatientsForNextUnlockedDateAndClearOldQueues(LocalDate.now().minusDays(1),
                patients);

        //then
        verify(patientRepository, times(6)).save(any());
        verify(queueRepository).save(emptyCurrentQueue);
        verify(queueRepository).save(fullUnlockedOldQueue);

        assertEquals(new ArrayList<>(), fullUnlockedOldQueue.getWaitingPatients());
        assertEquals(List.of(pOther2, pOther, pSurgery3), fullUnlockedOldQueue.getPatients());
        assertTrue(fullUnlockedOldQueue.isLocked());
        assertEquals(patients.size(), emptyCurrentQueue.getWaitingPatients().size());
        //new order: pUrgent2, pUrgent, pSurgery2, pSurgery, pOther3, pOther4
        assertEquals(emptyCurrentQueue.getPatients().get(0), pUrgent2);
        assertEquals(emptyCurrentQueue.getPatients().get(1), pUrgent);
        assertEquals(emptyCurrentQueue.getPatients().get(2), pSurgery2);
        assertEquals(emptyCurrentQueue.getPatients().get(3), pSurgery);
        assertEquals(emptyCurrentQueue.getPatients().get(4), pOther3);
        assertEquals(emptyCurrentQueue.getPatients().get(5), pOther4);
        assertEquals(0, pUrgent2.getPositionInQueue());
        assertEquals(1, pUrgent.getPositionInQueue());
        assertEquals(2, pSurgery2.getPositionInQueue());
        assertEquals(3, pSurgery.getPositionInQueue());
        assertEquals(4, pOther3.getPositionInQueue());
        assertEquals(5, pOther4.getPositionInQueue());
        assertEquals(emptyCurrentQueue, pUrgent2.getQueue());
        assertEquals(emptyCurrentQueue, pUrgent.getQueue());
        assertEquals(emptyCurrentQueue, pSurgery2.getQueue());
        assertEquals(emptyCurrentQueue, pSurgery.getQueue());
        assertEquals(emptyCurrentQueue, pOther3.getQueue());
        assertEquals(emptyCurrentQueue, pOther4.getQueue());
    }

    @Test
    void transferPatientsForNextUnlockedDateAndClearOldQueuesWhenNoUnlockedQueues() {
        //given
        List.of(pOther, pOther2, pSurgery3).forEach(p -> p.setStatus(psConfirmedTwice));

        Queue newQueue = Queue.builder().build();

        when(queueRepository.findQueuesByLockedFalseAndDateAfter(any())).thenReturn(Collections.emptyList());
        when(queueRepository.findQueuesByLockedTrueAndDateAfter(any())).thenReturn(List.of(fullLockedCurrentQueue,
                emptyCurrentQueue));
        when(queueRepository.findQueueByDate(LocalDate.of(2021, 12, 8)))
                .thenReturn(Optional.of(newQueue));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(psWaiting));

        fullLockedCurrentQueue.setDate(LocalDate.of(2021, 12, 6));
        emptyCurrentQueue.setDate(LocalDate.of(2021, 12, 7));

        List<Patient> patients = List.of(pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery);
        patients.forEach(p -> p.setQueue(fullUnlockedOldQueue));
        //when

        queueService.transferPatientsForNextUnlockedDateAndClearOldQueues(
                LocalDate.of(2021, 12, 5), patients);

        //then
        verify(patientRepository, times(6)).save(any());
        verify(queueRepository).save(newQueue);
        verify(queueRepository).save(fullUnlockedOldQueue);

        assertEquals(new ArrayList<>(), fullUnlockedOldQueue.getWaitingPatients());
        assertEquals(List.of(pOther2, pOther, pSurgery3), fullUnlockedOldQueue.getConfirmedPatients());
        assertTrue(fullUnlockedOldQueue.isLocked());
        assertEquals(patients.size(), newQueue.getPatients().size());
        //new order: pUrgent2, pUrgent, pSurgery2, pSurgery, pOther3, pOther4
        assertEquals(newQueue.getPatients().get(0), pUrgent2);
        assertEquals(newQueue.getPatients().get(1), pUrgent);
        assertEquals(newQueue.getPatients().get(2), pSurgery2);
        assertEquals(newQueue.getPatients().get(3), pSurgery);
        assertEquals(newQueue.getPatients().get(4), pOther3);
        assertEquals(newQueue.getPatients().get(5), pOther4);
        assertEquals(0, pUrgent2.getPositionInQueue());
        assertEquals(1, pUrgent.getPositionInQueue());
        assertEquals(2, pSurgery2.getPositionInQueue());
        assertEquals(3, pSurgery.getPositionInQueue());
        assertEquals(4, pOther3.getPositionInQueue());
        assertEquals(5, pOther4.getPositionInQueue());
        assertEquals(newQueue, pUrgent2.getQueue());
        assertEquals(newQueue, pUrgent.getQueue());
        assertEquals(newQueue, pSurgery2.getQueue());
        assertEquals(newQueue, pSurgery.getQueue());
        assertEquals(newQueue, pOther3.getQueue());
        assertEquals(newQueue, pOther4.getQueue());
    }

    @Test
    void lockQueueForDateIfNecessary() {
        List.of(pOther2, pOther, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2)
                .forEach(p -> p.setStatus(psConfirmedTwice));

        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullUnlockedAllConfirmedQueue));

        queueService.lockQueueForDateIfNecessary(LocalDate.now());

        verify(queueRepository).save(queueCapture.capture());

        assertTrue(queueCapture.getValue().isLocked());
    }

    @Test
    void lockQueueForDateIfNecessaryNotEmptyWaitingPatientsQueueUnlockedQueueExists() {
        //given
        List.of(pOther2, pOther, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2)
                .forEach(p -> p.setStatus(psConfirmedTwice));

        when(queueRepository.findQueuesByLockedFalseAndDateAfter(any())).thenReturn(List.of(emptyCurrentQueue));
        when(queueRepository.findQueuesByLockedTrueAndDateAfter(any())).thenReturn(List.of(fullLockedCurrentQueue));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(psWaiting));
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullUnlockedCurrentQueueWithWaiting));
        //when

        queueService.lockQueueForDateIfNecessary(LocalDate.now());
        //then

        verify(patientRepository).save(any());
        verify(queueRepository).save(emptyCurrentQueue);
        verify(queueRepository).save(fullUnlockedCurrentQueueWithWaiting);

        assertEquals(new ArrayList<>(), fullUnlockedCurrentQueueWithWaiting.getWaitingPatients());
        assertEquals(8, fullUnlockedCurrentQueueWithWaiting.getConfirmedPatients().size());
        assertTrue(fullUnlockedCurrentQueueWithWaiting.isLocked());
        assertEquals(1, emptyCurrentQueue.getPatients().size());
        assertEquals(emptyCurrentQueue.getPatients().get(0), pSurgery);
        assertEquals(0, pSurgery.getPositionInQueue());
        assertEquals(emptyCurrentQueue, pSurgery.getQueue());
    }

    @Test
    void lockQueueForDateIfNecessaryNotEmptyWaitingPatientsQueueNoUnlockedQueue() {
        //given
        List.of(pOther2, pOther, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2)
                .forEach(p -> p.setStatus(psConfirmedTwice));

        Queue newQueue = Queue.builder().build();
        when(queueRepository.findQueuesByLockedFalseAndDateAfter(any())).thenReturn(Collections.emptyList());
        when(queueRepository.findQueuesByLockedTrueAndDateAfter(any())).thenReturn(List.of(fullLockedCurrentQueue));
        when(patientStatusRepository.findPatientStatusByName(any())).thenReturn(Optional.of(psWaiting));
        when(queueRepository.findQueueByDate(LocalDate.of(2021, 12, 6)))
                .thenReturn(Optional.of(fullUnlockedCurrentQueueWithWaiting));
        when(queueRepository.findQueueByDate(LocalDate.of(2021, 12, 8)))
                .thenReturn(Optional.of(newQueue));

        fullUnlockedCurrentQueueWithWaiting.setDate(LocalDate.of(2021, 12, 6));
        fullLockedCurrentQueue.setDate(LocalDate.of(2021, 12, 7));
        //when

        queueService.lockQueueForDateIfNecessary(LocalDate.of(2021, 12, 6));
        //then

        verify(patientRepository).save(any());
        verify(queueRepository).save(newQueue);
        verify(queueRepository).save(fullUnlockedCurrentQueueWithWaiting);

        assertEquals(new ArrayList<>(), fullUnlockedCurrentQueueWithWaiting.getWaitingPatients());
        assertEquals(8, fullUnlockedCurrentQueueWithWaiting.getConfirmedPatients().size());
        assertTrue(fullUnlockedCurrentQueueWithWaiting.isLocked());
        assertEquals(1, newQueue.getPatients().size());
        assertEquals(newQueue.getPatients().get(0), pSurgery);
        assertEquals(0, pSurgery.getPositionInQueue());
        assertEquals(newQueue, pSurgery.getQueue());
    }

    @Test
    void shouldThrowExceptionWhenLockQueueForDateIfNecessaryQueueNotFound() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> queueService.lockQueueForDateIfNecessary(
                LocalDate.now()));

    }

    @Test
    void removePatientFromQueue() {
        //old order: pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery3

        when(queueRepository.findQueueByPatientsContains(pSurgery3)).thenReturn(Optional.of(fullCurrentQueue));

        queueService.removePatientFromQueue(pSurgery3);

        verify(patientRepository, times(7)).save(any());
        verify(queueRepository).save(any());
        //new order: pUrgent2, pUrgent, pSurgery2, pOther3, pOther4
        assertEquals(fullCurrentQueue.getPatients().get(0), pUrgent2);
        assertEquals(fullCurrentQueue.getPatients().get(1), pUrgent);
        assertEquals(fullCurrentQueue.getPatients().get(2), pSurgery2);
        assertEquals(fullCurrentQueue.getPatients().get(3), pOther);
        assertEquals(fullCurrentQueue.getPatients().get(4), pOther2);
        assertEquals(fullCurrentQueue.getPatients().get(5), pOther3);
        assertEquals(fullCurrentQueue.getPatients().get(6), pOther4);
        assertEquals(0, pUrgent2.getPositionInQueue());
        assertEquals(1, pUrgent.getPositionInQueue());
        assertEquals(2, pSurgery2.getPositionInQueue());
        assertEquals(3, pOther.getPositionInQueue());
        assertEquals(4, pOther2.getPositionInQueue());
        assertEquals(5, pOther3.getPositionInQueue());
        assertEquals(6, pOther4.getPositionInQueue());
    }

    @Test
    void shouldThrowExceptionWhenRemovePatientFromQueueNotFound() {

        when(queueRepository.findQueueByPatientsContains(pSurgery3)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> queueService.removePatientFromQueue(pSurgery3));

        verify(patientRepository, never()).save(any());
        verify(queueRepository, never()).save(any());
    }

    @Test
    void checkIfPatientIsInAQueueForDate() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(fullCurrentQueue));

        assertDoesNotThrow(() -> queueService.checkIfPatientIsInAQueueForDate(LocalDate.now().plusDays(3),
                pOther3));
    }

    @Test
    void shouldThrowExceptionWhenCheckIfPatientIsInAQueueForDateNoQueue() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> queueService.checkIfPatientIsInAQueueForDate(
                LocalDate.now().plusDays(3), pOther3));
    }

    @Test
    void shouldThrowExceptionWhenCheckIfPatientIsInAQueuePatientNotInAQueue() {
        when(queueRepository.findQueueByDate(any())).thenReturn(Optional.of(emptyCurrentQueue));

        assertThrows(BadRequestException.class, () -> queueService.checkIfPatientIsInAQueueForDate(
                LocalDate.now().plusDays(3), pOther3));
    }

    @Test
    void refreshQueue() {
        //old order: pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery3

        queueService.refreshQueue(fullCurrentQueue);

        verify(patientRepository, times(8)).save(any());
        //new order: pUrgent2, pUrgent, pSurgery3, pSurgery2, pOther3, pOther4
        assertEquals(fullCurrentQueue.getPatients().get(0), pUrgent2);
        assertEquals(fullCurrentQueue.getPatients().get(1), pUrgent);
        assertEquals(fullCurrentQueue.getPatients().get(2), pSurgery3);
        assertEquals(fullCurrentQueue.getPatients().get(3), pSurgery2);
        assertEquals(fullCurrentQueue.getPatients().get(4), pOther);
        assertEquals(fullCurrentQueue.getPatients().get(5), pOther2);
        assertEquals(fullCurrentQueue.getPatients().get(6), pOther3);
        assertEquals(fullCurrentQueue.getPatients().get(7), pOther4);
        assertEquals(0, pUrgent2.getPositionInQueue());
        assertEquals(1, pUrgent.getPositionInQueue());
        assertEquals(2, pSurgery3.getPositionInQueue());
        assertEquals(3, pSurgery2.getPositionInQueue());
        assertEquals(4, pOther.getPositionInQueue());
        assertEquals(5, pOther2.getPositionInQueue());
        assertEquals(6, pOther3.getPositionInQueue());
        assertEquals(7, pOther4.getPositionInQueue());
    }

    @Test
    void getWaitingPatientsForPastQueues() {
        when(queueRepository.findQueuesByDateBefore(any())).thenReturn(List.of(fullUnlockedOldQueue, emptyOldQueue));

        List<Patient> patients = queueService.getWaitingPatientsForPastQueues();

        assertEquals(fullUnlockedOldQueue.getWaitingPatients(), patients);
    }


    private void initPatients() {
        pUrgent = Patient.builder().id(1).urgent(true).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().plusDays(2)).build();
        pUrgent2 = Patient.builder().id(2).urgent(true).diseases(List.of(diseaseSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().minusDays(1)).build();
        pSurgery = Patient.builder().id(3).urgent(false).diseases(List.of(diseaseSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().plusDays(2)).build();
        pSurgery2 = Patient.builder().id(4).urgent(false).diseases(List.of(diseaseSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().minusDays(1)).build();
        pSurgery3 = Patient.builder().id(5).urgent(false).diseases(List.of(diseaseSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().minusDays(4)).build();
        pOther = Patient.builder().id(6).urgent(false).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().minusDays(4)).build();
        pOther2 = Patient.builder().id(7).urgent(false).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().minusDays(2)).build();
        pOther3 = Patient.builder().id(8).urgent(false).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().minusDays(1)).build();
        pOther4 = Patient.builder().id(9).urgent(false).diseases(List.of(diseaseNoSurgery)).status(psWaiting)
                .admissionDate(LocalDate.now().plusDays(2)).build();

    }

    private void initQueues() {
        emptyCurrentQueue = Queue.builder().date(LocalDate.now().plusDays(1)).locked(false)
                .patients(Collections.emptyList()).build();
        emptyOldQueue = Queue.builder().date(LocalDate.now().minusDays(6))
                .patients(Collections.emptyList()).build();
        fullCurrentQueue = Queue.builder().date(LocalDate.now().plusDays(3)).locked(false)
                .patients(List.of(pOther2, pOther, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery3)).build();
        fullLockedCurrentQueue = Queue.builder().date(LocalDate.now().plusDays(4)).locked(true)
                .patients(List.of(pOther2, pOther, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2))
                .build();
        fullUnlockedAllConfirmedQueue = Queue.builder().date(LocalDate.now().plusDays(4)).locked(false)
                .patients(List.of(pOther2, pOther, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2))
                .build();
        notFullCurrentQueue = Queue.builder().date(LocalDate.now().plusDays(5)).locked(false)
                .patients(List.of(pUrgent, pSurgery2, pOther2, pOther)).build();
        fullUnlockedOldQueue = Queue.builder().date(LocalDate.now().minusDays(2)).locked(false)
                .patients(List.of(pUrgent, pUrgent2, pOther3, pOther4, pSurgery2, pSurgery,pOther2, pOther, pSurgery3))
                .build();
        fullUnlockedCurrentQueueWithWaiting = Queue.builder().date(LocalDate.now().plusDays(4))
                .locked(false)
                .patients(List.of(pOther2, pOther, pSurgery, pSurgery3, pUrgent, pUrgent2, pOther3, pOther4, pSurgery2))
                .build();
    }
}
