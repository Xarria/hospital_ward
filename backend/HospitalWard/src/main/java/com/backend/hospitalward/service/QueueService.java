package com.backend.hospitalward.service;

import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.Queue;
import com.backend.hospitalward.model.common.PatientStatusName;
import com.backend.hospitalward.repository.BaseRepository;
import com.backend.hospitalward.repository.PatientRepository;
import com.backend.hospitalward.repository.PatientStatusRepository;
import com.backend.hospitalward.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
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
@Retryable(value = {PersistenceException.class, HibernateException.class, JDBCException.class, PessimisticLockingFailureException.class},
        exclude = ConstraintViolationException.class, backoff = @Backoff(delay = 1000))
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class QueueService {

    PatientRepository patientRepository;

    QueueRepository queueRepository;

    PatientStatusRepository patientStatusRepository;

    BaseRepository baseRepository;


    public List<Queue> getAllCurrentQueues() {
        return queueRepository.findQueuesByDateAfter(LocalDate.now().minusDays(1));
    }

    public Queue getQueueForDate(LocalDate date) {
        return queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
    }

    public List<LocalDate> findFullAdmissionDates() {
        return queueRepository.findAll().stream()
                .filter(queue -> queue.getPatients().size() >= 8)
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
                .patients(new ArrayList<>())
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
            return !queue.isLocked() && queue.getPatients().size() < 8;
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

        List<Patient> patients = new LinkedList<>(queue.getPatients());
        patients.add(patient);
        queue.setPatients(patients);

        patient.setQueue(queue);
        patientRepository.save(patient);

        refreshQueue(queue);
        queueRepository.save(queue);
    }

    public void switchPatientQueue(Patient patient, LocalDate newQueueDate) {
        Queue oldQueue = queueRepository.findQueueByPatientsContains(patient).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
        Queue newQueue = queueRepository.findQueueByDate(newQueueDate).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        List<Patient> oldQueuePatients = new LinkedList<>(oldQueue.getPatients());
        oldQueuePatients.remove(patient);
        oldQueue.setPatients(oldQueuePatients);

        List<Patient> newQueuePatients = new LinkedList<>(newQueue.getPatients());
        newQueuePatients.add(patient);
        newQueue.setPatients(newQueuePatients);

        patient.setQueue(newQueue);

        refreshQueue(oldQueue);
        refreshQueue(newQueue);

        changeQueueLockStatusIfNecessary(oldQueue.getDate());
        queueRepository.save(newQueue);
    }

    public void switchPatients(Patient urgentPatient, LocalDate date) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        Patient lowestPriorityPatient = queue.getConfirmedPatients().stream()
                .filter(p -> p.getId() != urgentPatient.getId())
                .max(Comparator.comparing(Patient::getPositionInQueue))
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

        List<Patient> queuePatients = new LinkedList<>(queue.getPatients());
        queuePatients.remove(lowestPriorityPatient);
        queue.setPatients(queuePatients);

        lowestPriorityPatient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));

        transferPatientToNextUnlockedQueue(lowestPriorityPatient, date);

        refreshQueue(queue);
        queueRepository.save(queue);
    }

    public void transferPatientToNextUnlockedQueue(Patient patient, LocalDate previousDate) {
        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalseAndDateAfter(previousDate);
        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrueAndDateAfter(previousDate);
        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
                .map(Queue::getDate)
                .collect(Collectors.toList());

        if (!unlockedQueues.isEmpty()) {
            Queue newQueue = unlockedQueues.stream()
                    .min(Comparator.comparing(Queue::getDate))
                    .get();
            baseRepository.pessimisticLock(newQueue);
            patient.setQueue(newQueue);
            if (newQueue.getPatients() != null && !newQueue.getPatients().isEmpty()) {
                List<Patient> newQueuePatients = new LinkedList<>(newQueue.getPatients());
                newQueuePatients.add(patient);
                newQueue.setPatients(newQueuePatients);
            } else {
                newQueue.setPatients(List.of(patient));
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
                createdQueue.setPatients(List.of(patient));
                refreshQueue(createdQueue);
                queueRepository.save(createdQueue);
                break;
            } else {
                date = date.plusDays(1);
            }
        }
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

    public void changeQueueLockStatusIfNecessary(LocalDate date) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        if (queue.getConfirmedPatients().size() >= 8) {
            queue.setLocked(true);
            if (queue.getWaitingPatients() != null && !queue.getWaitingPatients().isEmpty()) {
                transferPatientsFromLockedQueue(queue);
            }
        }
        else {
            queue.setLocked(false);
        }
        queueRepository.save(queue);
    }

    private void transferPatientsFromLockedQueue(Queue lockedQueue) {
        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalseAndDateAfter(lockedQueue.getDate());
        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrueAndDateAfter(lockedQueue.getDate());
        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
                .map(Queue::getDate)
                .collect(Collectors.toList());

        if (unlockedQueues.size() > 1) {
            unlockedQueues = unlockedQueues.stream()
                    .sorted(Comparator.comparing(Queue::getDate))
                    .collect(Collectors.toList());
        }

        Queue newQueue = findNewQueue(lockedQueue.getDate(), unlockedQueues, lockedQueuesDates);

        lockedQueue.getWaitingPatients().forEach(patient -> patient.setQueue(newQueue));
        lockedQueue.getWaitingPatients().forEach(patient -> patient.setStatus(patientStatusRepository
                .findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND))));

        if (newQueue.getPatients() != null && !newQueue.getPatients().isEmpty()) {
            List<Patient> newQueuePatients = new LinkedList<>(newQueue.getPatients());
            newQueuePatients.addAll(lockedQueue.getWaitingPatients());
            newQueue.setPatients(newQueuePatients);
        } else {
            newQueue.setPatients(lockedQueue.getWaitingPatients());
        }

        List<Patient> lockedQueuePatients = new LinkedList<>(lockedQueue.getPatients());
        lockedQueuePatients.removeAll(lockedQueue.getWaitingPatients());
        lockedQueue.setPatients(lockedQueuePatients);
        refreshQueue(newQueue);
        queueRepository.save(newQueue);
    }

    public void removePatientFromQueue(Patient patient) {
        Queue queue = queueRepository.findQueueByPatientsContains(patient).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        List<Patient> patients = new LinkedList<>(queue.getPatients());
        patients.remove(patient);

        queue.setPatients(patients);

        refreshQueue(queue);
        queueRepository.save(queue);
    }

    public void checkIfPatientIsInAQueueForDate(LocalDate date, Patient patient) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
        if (!queue.getPatients().contains(patient)) {
            throw new BadRequestException(ErrorKey.PATIENT_WRONG_QUEUE);
        }
    }

    public void refreshQueue(Queue queue) {
            queue.setPatients(calculatePatientsPositions(queue));
    }

    public void refreshQueueAfterUpdate(Queue queue) {
        baseRepository.pessimisticLock(queue);
        refreshQueue(queue);
        queueRepository.save(queue);
    }

    public List<Patient> getWaitingPatientsForPastQueues() {
        List<Queue> pastQueues = queueRepository.findQueuesByDateBefore(LocalDate.now());

        return pastQueues.stream()
                .map(Queue::getWaitingPatients)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Patient> calculatePatientsPositions(Queue queue) {
        List<Patient> patients = new LinkedList<>(queue.getPatients());

        if (patients.isEmpty()) {
            return patients;
        }

        if (patients.size() == 1) {
            rewritePatientsPositions(patients);
            return patients;
        }

        List<Patient> sortedUrgentPatients = patients.stream()
                .filter(Patient::isUrgent)
                .sorted(this::compareDates)
                .collect(Collectors.toCollection(LinkedList::new));

        patients.removeAll(sortedUrgentPatients);

        List<Patient> sortedCathererOrSurgeryPatients = patients.stream()
                .filter(patient -> patient.getDiseases().stream()
                        .anyMatch(disease -> disease.isCathererRequired() || disease.isSurgeryRequired()))
                .sorted(this::compareDates)
                .collect(Collectors.toCollection(LinkedList::new));

        patients.removeAll(sortedCathererOrSurgeryPatients);

        List<Patient> otherPatients = patients.stream()
                .sorted(this::compareDates)
                .collect(Collectors.toCollection(LinkedList::new));

        List<Patient> allSortedPatients = new LinkedList<>();

        allSortedPatients.addAll(sortedUrgentPatients);
        allSortedPatients.addAll(sortedCathererOrSurgeryPatients);
        allSortedPatients.addAll(otherPatients);

        rewritePatientsPositions(allSortedPatients);

        return allSortedPatients;

    }

    private int compareDates(Patient p1, Patient p2) {
        if (p1.getAdmissionDate().compareTo(p2.getAdmissionDate()) == 0) {
            return p1.getCreationDate().compareTo(p2.getCreationDate());
        } else {
            return p1.getAdmissionDate().compareTo(p2.getAdmissionDate());
        }
    }

    private void rewritePatientsPositions(List<Patient> allSortedPatients) {
        for (int i = 0; i < allSortedPatients.size(); i++) {
            Long id = allSortedPatients.get(i).getId();

            Patient patient = patientRepository.findPatientById(id).orElseThrow(()
                    -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

            patient.setPositionInQueue(i);
            baseRepository.detach(patient);
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
