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
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
        return queueRepository.findQueuesByDateAfter(Date.valueOf(LocalDate.now().minusDays(1)));
    }

    public Queue getQueueForDate(Date date) {
        return queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
    }

    public List<LocalDate> findFullAdmissionDates() {
        return queueRepository.findAll().stream()
                .filter(queue -> queue.getAllPatients().size() >= 8)
                .map(queue -> queue.getDate().toLocalDate())
                .collect(Collectors.toList());
    }

    public void createQueueForDateIfNotExists(Date date) {
        if (checkIfDateIsWeekend(date.toLocalDate())) {
            throw new ConflictException(ErrorKey.ADMISSION_DATE_WEEKEND);
        }
        if (checkIfQueueForDateExists(date)) {
            return;
        }
        queueRepository.save(Queue.builder()
                .date(date)
                .patientsWaiting(new ArrayList<>())
                .patientsConfirmed(new ArrayList<>())
                .build());
    }

    public boolean checkIfQueueForDateExists(Date date) {
        return queueRepository.findQueueByDate(date).isPresent();
    }

    public boolean checkIfPatientCanBeAddedForDate(Date date) {
        Optional<Queue> queueForDate = queueRepository.findQueueByDate(date);
        if (queueForDate.isPresent()) {
            Queue queue = queueForDate.get();
            return !queue.isLocked() && queue.getAllPatients().size() < 8;
        } else {
            createQueueForDateIfNotExists(date);
            return true;
        }
    }

    public boolean checkIfQueueForDateIsLocked(Date date) {
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

        queue.getPatientsWaiting().add(patient);

        refreshQueue(queue);

        queueRepository.save(queue);
    }

    public void switchPatientQueue(Patient patient, Date newQueueDate) {
        Queue oldQueue = queueRepository.findQueueByPatientsWaitingContains(patient).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
        Queue newQueue = queueRepository.findQueueByDate(newQueueDate).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        oldQueue.getPatientsWaiting().remove(patient);
        oldQueue.getPatientsConfirmed().remove(patient);

        newQueue.getPatientsWaiting().add(patient);

        refreshQueue(oldQueue);
        refreshQueue(newQueue);

        queueRepository.save(oldQueue);
        queueRepository.save(newQueue);
    }

    public void switchPatients(Patient urgentPatient, Date date) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        Patient lowestPriorityPatient = queue.getPatientsConfirmed().stream()
                .filter(patient -> patient.getPositionInQueue() == queue.getPatientsConfirmed().size() - 1)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

        queue.getPatientsConfirmed().remove(lowestPriorityPatient);
        queue.getPatientsConfirmed().add(urgentPatient);

        urgentPatient.setPositionInQueue(lowestPriorityPatient.getPositionInQueue());
        urgentPatient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_TWICE.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));

        lowestPriorityPatient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));

        transferPatientToNextUnlockedQueue(lowestPriorityPatient, date);

        patientRepository.save(lowestPriorityPatient);

        refreshQueue(queue);

        queueRepository.save(queue);
    }

    public void transferPatientToNextUnlockedQueue(Patient patient, Date previousDate) {
        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalse();
        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrue();
        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
                .map(queue -> queue.getDate().toLocalDate())
                .collect(Collectors.toList());

        if (!unlockedQueues.isEmpty()) {
            unlockedQueues.sort(Comparator.comparing(Queue::getDate));
            patient.setQueue(unlockedQueues.get(0));
            refreshQueue(unlockedQueues.get(0));
            queueRepository.save(unlockedQueues.get(0));
            return;
        }

        LocalDate date = previousDate.toLocalDate().plusDays(1);

        while (true) {
            if (checkIfDateIsSaturday(date)) {
                date = date.plusDays(2);
            } else if (checkIfDateIsSunday(date)) {
                date = date.plusDays(1);
            }

            if (!lockedQueuesDates.contains(date)) {
                createQueueForDateIfNotExists(Date.valueOf(date));
                Queue createdQueue = queueRepository.findQueueByDate(Date.valueOf(date)).orElseThrow(()
                        -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
                patient.setQueue(createdQueue);
                createdQueue.getPatientsWaiting().add(patient);
                patientRepository.save(patient);
                queueRepository.save(createdQueue);
                break;
            } else {
                date = date.plusDays(1);
            }
        }
        //TODO email
    }

    public void transferPatientsForNextUnlockedDateAndClearOldQueues(Date previousDate, List<Patient> patients) {
        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalse();
        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrue();
        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
                .map(queue -> queue.getDate().toLocalDate())
                .collect(Collectors.toList());

        unlockedQueues.sort(Comparator.comparing(Queue::getDate));

        Queue newQueue = findNewQueue(previousDate, unlockedQueues, lockedQueuesDates);

        List<Queue> oldQueues = patients.stream().map(Patient::getQueue).distinct().collect(Collectors.toList());

        patients.forEach(patient -> patient.setQueue(newQueue));
        patients.forEach(patient -> patient.setStatus(patientStatusRepository
                .findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND))));

        oldQueues.forEach(queue -> queue.setPatientsWaiting(new ArrayList<>()));
        oldQueues.forEach(queue -> queue.setLocked(true));

        patients.forEach(patientRepository::save);
        oldQueues.forEach(queueRepository::save);

    }

    private Queue findNewQueue(Date previousDate, List<Queue> unlockedQueues, List<LocalDate> lockedQueuesDates) {
        Queue newQueue;
        if (!unlockedQueues.isEmpty()) {
            return unlockedQueues.get(0);
        }

        LocalDate date = previousDate.toLocalDate().plusDays(1);

        while (true) {
            if (checkIfDateIsSaturday(date)) {
                date = date.plusDays(2);
            } else if (checkIfDateIsSunday(date)) {
                date = date.plusDays(1);
            }

            if (!lockedQueuesDates.contains(date)) {
                createQueueForDateIfNotExists(Date.valueOf(date));
                newQueue = queueRepository.findQueueByDate(Date.valueOf(date)).orElseThrow(()
                        -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
                return newQueue;
            } else {
                date = date.plusDays(1);
            }
        }
    }

    public void lockQueueForDateIfNecessary(Date date) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
        if (queue.getPatientsConfirmed().size() >= 8) {
            queue.setLocked(true);
            transferPatientsFromLockedQueue(queue);
            queueRepository.save(queue);
        }
    }

    private void transferPatientsFromLockedQueue(Queue lockedQueue) {
        List<Queue> unlockedQueues = queueRepository.findQueuesByLockedFalse();
        List<Queue> lockedQueues = queueRepository.findQueuesByLockedTrue();
        List<LocalDate> lockedQueuesDates = lockedQueues.stream()
                .map(queue -> queue.getDate().toLocalDate())
                .collect(Collectors.toList());

        unlockedQueues.sort(Comparator.comparing(Queue::getDate));

        Queue newQueue = findNewQueue(lockedQueue.getDate(), unlockedQueues, lockedQueuesDates);

        lockedQueue.getPatientsWaiting().forEach(patient -> patient.setQueue(newQueue));
        lockedQueue.getPatientsWaiting().forEach(patient -> patient.setStatus(patientStatusRepository
                .findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND))));

        lockedQueue.setPatientsWaiting(new ArrayList<>());

        lockedQueue.getPatientsWaiting().forEach(patientRepository::save);
    }

    public void confirmPatient(Patient patient, LocalDate date) {
        Queue queue = queueRepository.findQueueByDate(Date.valueOf(date)).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        queue.getPatientsWaiting().remove(patient);
        queue.getPatientsConfirmed().add(patient);

        refreshQueue(queue);

        queueRepository.save(queue);
    }

    public void removePatientFromQueue(Patient patient) {
        Queue queue = queueRepository.findQueueByPatientsWaitingContains(patient).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));

        queue.getPatientsWaiting().remove(patient);

        refreshQueue(queue);

        queueRepository.save(queue);
    }

    public void checkIfPatientIsInAQueueForDate(Date date, Patient patient) {
        Queue queue = queueRepository.findQueueByDate(date).orElseThrow(()
                -> new NotFoundException(ErrorKey.QUEUE_NOT_FOUND));
        if (!queue.getPatientsWaiting().contains(patient)) {
            throw new BadRequestException(ErrorKey.PATIENT_WRONG_QUEUE);
        }
    }

    public void refreshQueue(Queue queue) {
        queue.setPatientsWaiting(calculatePatientsPositions(queue));
    }

    public List<Patient> getWaitingPatientsForPastQueues() {
        List<Queue> pastQueues = queueRepository.findQueuesByDateBefore(Date.valueOf(LocalDate.now()));

       return pastQueues.stream()
                .map(Queue::getPatientsWaiting)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Patient> calculatePatientsPositions(Queue queue) {
        List<Patient> waitingPatients = queue.getPatientsWaiting();

        List<Patient> sortedUrgentPatients = waitingPatients.stream()
                .filter(Patient::isUrgent)
                .sorted(Comparator.comparing(Patient::getAdmissionDate))
                .collect(Collectors.toList());

        waitingPatients.removeAll(sortedUrgentPatients);

        List<Patient> sortedCathererOrSurgeryPatients = waitingPatients.stream()
                .filter(patient -> patient.getDiseases().stream()
                        .anyMatch(disease -> disease.isCathererRequired() || disease.isSurgeryRequired()))
                .sorted(Comparator.comparing(Patient::getAdmissionDate))
                .collect(Collectors.toList());

        waitingPatients.removeAll(sortedCathererOrSurgeryPatients);

        List<Patient> otherPatients = waitingPatients.stream()
                .sorted(Comparator.comparing(Patient::getAdmissionDate))
                .collect(Collectors.toList());

        List<Patient> allSortedPatients = new ArrayList<>();

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
