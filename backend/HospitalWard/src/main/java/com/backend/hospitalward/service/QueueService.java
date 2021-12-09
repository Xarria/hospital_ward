package com.backend.hospitalward.service;

import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.Queue;
import com.backend.hospitalward.model.common.PatientStatusName;
import com.backend.hospitalward.repository.PatientRepository;
import com.backend.hospitalward.repository.PatientStatusRepository;
import com.backend.hospitalward.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Retryable(value = {PersistenceException.class, HibernateException.class, JDBCException.class},
        exclude = ConstraintViolationException.class, backoff = @Backoff(delay = 1000))
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class QueueService {

    PatientRepository patientRepository;

    QueueRepository queueRepository;

    PatientStatusRepository patientStatusRepository;


    public List<Queue> getAllCurrentQueues() {
        return queueRepository.findQueuesByDateAfter(LocalDate.now().minusDays(1));
    }

    public Queue getQueueForDate(LocalDate date) {
        return queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
    }

    public List<LocalDate> findFullAdmissionDates() {
        return queueRepository.findAll().stream()
                .filter(queue -> queue.getAllPatients().size() >= 8)
                .map(Queue::getDate)
                .collect(Collectors.toList());
    }

    public void createQueueForDateIfNotExists(LocalDate date) {
        if (checkIfDateIsWeekend(date)) {
            throw new ConflictException(ErrorKey.ADMISSION_DATE_WEEKEND);
        }
        if (checkIfQueueForDateExists(date)) {
            return;
        }
        queueRepository.save(Queue.builder()
                .date(date)
                .patientsConfirmed(new ArrayList<>())
                .patientsWaiting(new ArrayList<>())
                .locked(false)
                .build());
    }

    public boolean checkIfQueueForDateExists(LocalDate date) {
        return queueRepository.findQueueByDate(date).isPresent();
    }

    public boolean checkIfPatientCanBeAddedForDate(LocalDate date) {
        Optional<Queue> queueForDate = queueRepository.findQueueByDate(date);
        if (queueForDate.isPresent()) {
            Queue queue = queueForDate.get();
            return !queue.isLocked() && queue.getAllPatients().size() < 8;
        } else {
            createQueueForDateIfNotExists(date);
            return true;
        }
    }

    public boolean checkIfQueueForDateIsLocked(LocalDate date) {
        Optional<Queue> queueForDate = queueRepository.findQueueByDate(date);
        if (queueForDate.isPresent()) {
            Queue queue = queueForDate.get();
            return queue.isLocked();
        } else {
            createQueueForDateIfNotExists(date);
            return false;
        }
    }

    public void addPatientToQueue(Patient patient) {
        createQueueForDateIfNotExists(patient.getAdmissionDate());

        Queue queue = queueRepository.findQueueByDate(patient.getAdmissionDate()).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        List<Patient> waitingPatients = new LinkedList<>(queue.getPatientsWaiting());
        waitingPatients.add(patient);
        queue.setPatientsWaiting(waitingPatients);

        patient.setQueue(queue);
        patientRepository.save(patient);

        refreshQueue(queue);

        queueRepository.save(queue);
    }

    public void switchPatientQueue(Patient patient, LocalDate newQueueDate) {
        Queue oldQueue = queueRepository.findQueueByPatientsWaitingContains(patient).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
        Queue newQueue = queueRepository.findQueueByDate(newQueueDate).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        List<Patient> oldQueueWaitingPatients = new LinkedList<>(oldQueue.getPatientsWaiting());
        oldQueueWaitingPatients.remove(patient);
        oldQueue.setPatientsWaiting(oldQueueWaitingPatients);
        List<Patient> oldQueueConfirmedPatients = new LinkedList<>(oldQueue.getPatientsConfirmed());
        oldQueueConfirmedPatients.remove(patient);
        oldQueue.setPatientsConfirmed(oldQueueConfirmedPatients);

        List<Patient> newQueueWaitingPatients = new LinkedList<>(newQueue.getPatientsWaiting());
        newQueueWaitingPatients.add(patient);
        newQueue.setPatientsWaiting(newQueueWaitingPatients);

        patient.setQueue(newQueue);

        refreshQueue(oldQueue);
        refreshQueue(newQueue);

        queueRepository.save(oldQueue);
        queueRepository.save(newQueue);
    }

    public void switchPatients(Patient urgentPatient, LocalDate date) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        Patient lowestPriorityPatient = queue.getPatientsConfirmed().stream()
                .filter(patient -> patient.getPositionInQueue() == queue.getPatientsConfirmed().size() - 1)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

        List<Patient> queuePatientsConfirmed = new LinkedList<>(queue.getPatientsConfirmed());
        queuePatientsConfirmed.remove(lowestPriorityPatient);
        queuePatientsConfirmed.add(urgentPatient);
        queue.setPatientsConfirmed(queuePatientsConfirmed);

        urgentPatient.setPositionInQueue(lowestPriorityPatient.getPositionInQueue());
        urgentPatient.setQueue(queue);
        urgentPatient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_TWICE.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));

        lowestPriorityPatient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));

        transferPatientToNextUnlockedQueue(lowestPriorityPatient, date);

        queueRepository.save(queue);
    }

    public void transferPatientToNextUnlockedQueue(Patient patient, LocalDate previousDate) {
        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalseAndDateAfter(LocalDate.now());
        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrueAndDateAfter(LocalDate.now());
        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
                .map(Queue::getDate)
                .collect(Collectors.toList());

        if (!unlockedQueues.isEmpty()) {
            unlockedQueues = unlockedQueues.stream()
                    .sorted(Comparator.comparing(Queue::getDate))
                    .collect(Collectors.toList());
            Queue newQueue = unlockedQueues.get(0);
            patient.setQueue(newQueue);
            if (newQueue.getPatientsWaiting() != null && !newQueue.getPatientsWaiting().isEmpty()) {
                List<Patient> newQueueWaitingPatients = new LinkedList<>(newQueue.getPatientsWaiting());
                newQueueWaitingPatients.add(patient);
                newQueue.setPatientsWaiting(newQueueWaitingPatients);
            } else {
                newQueue.setPatientsWaiting(List.of(patient));
            }
            refreshQueue(newQueue);
            queueRepository.save(newQueue);
            return;
        }

        LocalDate date = previousDate.plusDays(1);

        while (true) {
            if (checkIfDateIsSaturday(date)) {
                date = date.plusDays(2);
            } else if (checkIfDateIsSunday(date)) {
                date = date.plusDays(1);
            }

            if (!lockedQueuesDates.contains(date)) {
                createQueueForDateIfNotExists(date);
                Queue createdQueue = queueRepository.findQueueByDate(date).orElseThrow(()
                        -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
                patient.setQueue(createdQueue);
                createdQueue.setPatientsWaiting(List.of(patient));
                refreshQueue(createdQueue);
                queueRepository.save(createdQueue);
                break;
            } else {
                date = date.plusDays(1);
            }
        }
    }

    //TODO spytać czy transfer może być na szybciej niż 2 tygodnie
    public void transferPatientsForNextUnlockedDateAndClearOldQueues(LocalDate previousDate, List<Patient> patients) {
        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalseAndDateAfter(LocalDate.now());
        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrueAndDateAfter(LocalDate.now());
        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
                .map(Queue::getDate)
                .collect(Collectors.toList());

        if (unlockedQueues.size() > 1) {
            unlockedQueues = unlockedQueues.stream()
                    .sorted(Comparator.comparing(Queue::getDate))
                    .collect(Collectors.toList());
        }

        Queue newQueue = findNewQueue(previousDate, unlockedQueues, lockedQueuesDates);

        List<Queue> oldQueues = patients.stream().map(Patient::getQueue).distinct().collect(Collectors.toList());

        patients.forEach(patient -> patient.setQueue(newQueue));
        patients.forEach(patient -> patient.setStatus(patientStatusRepository
                .findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND))));

        oldQueues.forEach(queue -> queue.setPatientsWaiting(new ArrayList<>()));
        oldQueues.forEach(queue -> queue.setLocked(true));

        if (newQueue.getPatientsWaiting() != null && !newQueue.getPatientsWaiting().isEmpty()) {
            List<Patient> newQueuePatients = new LinkedList<>(newQueue.getPatientsWaiting());
            newQueuePatients.addAll(patients);
            newQueue.setPatientsWaiting(newQueuePatients);
        } else {
            newQueue.setPatientsWaiting(patients);
        }

        refreshQueue(newQueue);

        queueRepository.save(newQueue);
        oldQueues.forEach(queueRepository::save);

    }

    private Queue findNewQueue(LocalDate previousDate, List<Queue> unlockedQueues, List<LocalDate> lockedQueuesDates) {
        Queue newQueue;
        if (!unlockedQueues.isEmpty()) {
            return unlockedQueues.get(0);
        }

        LocalDate date = previousDate.plusDays(1);

        while (true) {
            if (checkIfDateIsSaturday(date)) {
                date = date.plusDays(2);
            } else if (checkIfDateIsSunday(date)) {
                date = date.plusDays(1);
            }

            if (!lockedQueuesDates.contains(date)) {
                createQueueForDateIfNotExists(date);
                newQueue = queueRepository.findQueueByDate(date).orElseThrow(()
                        -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
                return newQueue;
            } else {
                date = date.plusDays(1);
            }
        }
    }

    public void lockQueueForDateIfNecessary(LocalDate date) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
        if (queue.isLocked()) {
            return;
        }
        if (queue.getPatientsConfirmed().size() >= 8) {
            queue.setLocked(true);
            if (queue.getPatientsWaiting() != null && !queue.getPatientsWaiting().isEmpty()) {
                transferPatientsFromLockedQueue(queue);
            }
            queueRepository.save(queue);
        }
    }

    private void transferPatientsFromLockedQueue(Queue lockedQueue) {
        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalseAndDateAfter(LocalDate.now());
        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrueAndDateAfter(LocalDate.now());
        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
                .map(queue -> queue.getDate())
                .collect(Collectors.toList());

        if (unlockedQueues.size() > 1) {
            unlockedQueues = unlockedQueues.stream()
                    .sorted(Comparator.comparing(Queue::getDate))
                    .collect(Collectors.toList());
        }

        Queue newQueue = findNewQueue(lockedQueue.getDate(), unlockedQueues, lockedQueuesDates);

        lockedQueue.getPatientsWaiting().forEach(patient -> patient.setQueue(newQueue));
        lockedQueue.getPatientsWaiting().forEach(patient -> patient.setStatus(patientStatusRepository
                .findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND))));

        if (newQueue.getPatientsWaiting() != null && !newQueue.getPatientsWaiting().isEmpty()) {
            List<Patient> newQueuePatients = new LinkedList<>(newQueue.getPatientsWaiting());
            newQueuePatients.addAll(lockedQueue.getPatientsWaiting());
            newQueue.setPatientsWaiting(newQueuePatients);
        } else {
            newQueue.setPatientsWaiting(lockedQueue.getPatientsWaiting());
        }

        lockedQueue.setPatientsWaiting(new ArrayList<>());
        refreshQueue(newQueue);
        queueRepository.save(newQueue);
    }

    public void confirmPatient(Patient patient, LocalDate date) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        List<Patient> waitingPatients = new LinkedList<>(queue.getPatientsWaiting());
        List<Patient> confirmedPatients = new LinkedList<>(queue.getPatientsConfirmed());

        waitingPatients.remove(patient);
        confirmedPatients.add(patient);

        queue.setPatientsWaiting(waitingPatients);
        queue.setPatientsConfirmed(confirmedPatients);

        refreshQueue(queue);

        queueRepository.save(queue);
    }

    public void removePatientFromQueue(Patient patient) {
        Queue queue = queueRepository.findQueueByPatientsWaitingContains(patient).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        List<Patient> waitingPatients = new LinkedList<>(queue.getPatientsWaiting());
        waitingPatients.remove(patient);

        queue.setPatientsWaiting(waitingPatients);

        refreshQueue(queue);

        queueRepository.save(queue);
    }

    public void checkIfPatientIsInAQueueForDate(LocalDate date, Patient patient) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
        if (!queue.getPatientsWaiting().contains(patient)) {
            throw new BadRequestException(ErrorKey.PATIENT_WRONG_QUEUE);
        }
    }

    public void refreshQueue(Queue queue) {
        if (!queue.isLocked()) {
            queue.setPatientsWaiting(calculatePatientsPositions(queue));
        }
    }

    public List<Patient> getWaitingPatientsForPastQueues() {
        List<Queue> pastQueues = queueRepository.findQueuesByDateBefore(LocalDate.now());

        return pastQueues.stream()
                .map(Queue::getPatientsWaiting)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Patient> calculatePatientsPositions(Queue queue) {
        List<Patient> waitingPatients = new LinkedList<>(queue.getPatientsWaiting());

        if (waitingPatients.isEmpty()) {
            return waitingPatients;
        }

        if (waitingPatients.size() == 1) {
            rewritePatientsPositions(waitingPatients);
            return waitingPatients;
        }

        List<Patient> sortedUrgentPatients = waitingPatients.stream()
                .filter(Patient::isUrgent)
                .sorted(Comparator.comparing(Patient::getAdmissionDate))
                .collect(Collectors.toCollection(LinkedList::new));

        waitingPatients.removeAll(sortedUrgentPatients);

        List<Patient> sortedCathererOrSurgeryPatients = waitingPatients.stream()
                .filter(patient -> patient.getDiseases().stream()
                        .anyMatch(disease -> disease.isCathererRequired() || disease.isSurgeryRequired()))
                .sorted(Comparator.comparing(Patient::getAdmissionDate))
                .collect(Collectors.toCollection(LinkedList::new));

        waitingPatients.removeAll(sortedCathererOrSurgeryPatients);

        List<Patient> otherPatients = waitingPatients.stream()
                .sorted(Comparator.comparing(Patient::getAdmissionDate))
                .collect(Collectors.toCollection(LinkedList::new));

        List<Patient> allSortedPatients = new LinkedList<>();

        allSortedPatients.addAll(sortedUrgentPatients);
        allSortedPatients.addAll(sortedCathererOrSurgeryPatients);
        allSortedPatients.addAll(otherPatients);

        rewritePatientsPositions(allSortedPatients);

        return allSortedPatients;

    }

    private void rewritePatientsPositions(List<Patient> allSortedPatients) {
        for (int i = 0; i < allSortedPatients.size(); i++) {
            Long id = allSortedPatients.get(i).getId();

            Patient patient = patientRepository.findPatientById(id).orElseThrow(()
                    -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

            patient.setPositionInQueue(i);
            patientRepository.save(patient);
        }
    }

    private boolean checkIfDateIsSaturday(LocalDate date) {
        return date.get(ChronoField.DAY_OF_WEEK) == 6;
    }

    private boolean checkIfDateIsSunday(LocalDate date) {
        return date.get(ChronoField.DAY_OF_WEEK) == 7;
    }

    private boolean checkIfDateIsWeekend(LocalDate date) {
        return checkIfDateIsSunday(date) || checkIfDateIsSaturday(date);
    }
}
