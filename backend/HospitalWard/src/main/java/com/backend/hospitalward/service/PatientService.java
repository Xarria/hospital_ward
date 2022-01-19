package com.backend.hospitalward.service;

import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.Disease;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.common.AccessLevelName;
import com.backend.hospitalward.model.common.PatientStatusName;
import com.backend.hospitalward.repository.*;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Retryable(value = {PersistenceException.class, HibernateException.class, JDBCException.class},
        exclude = ConstraintViolationException.class, backoff = @Backoff(delay = 1000))
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class PatientService {

    PatientRepository patientRepository;

    AccountRepository accountRepository;

    DiseaseRepository diseaseRepository;

    CovidStatusRepository covidStatusRepository;

    PatientTypeRepository patientTypeRepository;

    PatientStatusRepository patientStatusRepository;

    BaseRepository baseRepository;

    QueueService queueService;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findPatientById(id).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));
    }

    public void createPatient(Patient patient, String createdBy, List<String> diseases, String mainDoctorLogin,
                              String covidStatus) {
        if (checkIfDateIsWeekendOrFriday(patient.getAdmissionDate()) ||
                !checkIfDateIsAtLeastTwoWeeksFromToday(patient.getAdmissionDate())) {
            throw new ConflictException(ErrorKey.INVALID_ADMISSION_DATE);
        }

        if (patient.getPhoneNumber() == null && patient.getEmailAddress() == null) {
            throw new ConflictException(ErrorKey.CONTACT_INFO_REQUIRED);
        }

        if (patient.getReferralDate() == null && (patient.getReferralNr() == null || patient.getReferralNr().isEmpty())) {
            throw new ConflictException(ErrorKey.REFERRAL_INFO_REQUIRED);
        }

        setMainDoctor(patient, mainDoctorLogin);
        patient.setVersion(0L);
        patient.setCreationDate(Timestamp.from(Instant.now()));
        if (patient.isUrgent() && createdBy == null) {
            throw new ConflictException(ErrorKey.NO_PERMISSION_TO_CREATE_URGENT_PATIENT);
        }


        Account createdByAccount = accountRepository.findAccountByLogin(createdBy).orElseThrow(()
                -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        if (patient.isUrgent() && !hasPermissionToCreateUrgentPatient(createdByAccount)) {
            throw new ConflictException(ErrorKey.NO_PERMISSION_TO_CREATE_URGENT_PATIENT);
        }
        patient.setCreatedBy(createdByAccount);


        patient.setDiseases(getDiseasesFromDatabase(diseases));
        patient.setCovidStatus(covidStatusRepository.findCovidStatusByStatus(covidStatus).orElseThrow(()
                -> new NotFoundException(ErrorKey.COVID_STATUS_NOT_FOUND)));

        patient.setPatientType(patientTypeRepository.findPatientTypeByName(patient.findPatientType()).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_TYPE_NOT_FOUND)));

        patient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));

        if (!patient.isUrgent()) {
            if (!queueService.checkIfPatientCanBeAddedForDate(patient.getAdmissionDate())) {
                throw new ConflictException(ErrorKey.QUEUE_LOCKED_OR_FULL);
            }
        }

        queueService.addPatientToQueue(patient);
    }

    public void updatePatient(long id, Patient patient, List<String> diseases, String mainDoctor, String covidStatus,
                              String requestedBy) {
        Patient patientFromDB = patientRepository.findPatientById(id).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

        baseRepository.detach(patientFromDB);

        setPatientFields(patient, diseases, mainDoctor, covidStatus, patientFromDB, requestedBy);

        patientRepository.save(patientFromDB);

        queueService.refreshQueueAfterUpdate(patientFromDB.getQueue());
    }


    public void confirmPatient(Long id) {
        Patient patient = patientRepository.findPatientById(id).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

        if (patient.getStatus().getName().equals(PatientStatusName.CONFIRMED_TWICE.name())) {
            throw new ConflictException(ErrorKey.PATIENT_CONFIRMED);
        }

        setPatientStatus(patient);

        LocalDate queueDate = patient.getQueue().getDate();

        if (patient.getStatus().getName().equals(PatientStatusName.CONFIRMED_TWICE.name())) {

            if (queueService.checkIfQueueForDateIsLocked(queueDate)) {
                if (patient.isUrgent()) {
                    queueService.switchPatients(patient, queueDate);
                } else {
                    throw new ConflictException(ErrorKey.QUEUE_LOCKED);
                }
            }
            patient = patientRepository.findPatientById(id).orElseThrow(()
                    -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));
            patient.setAdmissionDate(queueDate);
            queueService.changeQueueLockStatusIfNecessary(queueDate);
        }
        baseRepository.detach(patient);
        patientRepository.save(patient);
    }

    public void changePatientAdmissionDate(Long id, LocalDate date, String modifiedBy) {
        if (checkIfDateIsWeekendOrFriday(date)) {
            throw new ConflictException(ErrorKey.ADMISSION_DATE_WEEKEND);
        }

        Patient patient = patientRepository.findPatientById(id).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

        if (patient.getStatus().getName().equals(PatientStatusName.CONFIRMED_TWICE.name())
                && patient.getAdmissionDate().isBefore(LocalDate.now())) {
            throw new BadRequestException(ErrorKey.PATIENT_ALREADY_ADMITTED);
        }

        if (!queueService.checkIfPatientCanBeAddedForDate(date)) {
            if (!patient.isUrgent()) {
                throw new ConflictException(ErrorKey.QUEUE_LOCKED_OR_FULL);
            }
        }

        patient.setModificationDate(Timestamp.from(Instant.now()));
        patient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.WAITING.name())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));
        patient.setModifiedBy(accountRepository.findAccountByLogin(modifiedBy).orElseThrow(()
                -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND)));

        queueService.switchPatientQueue(patient, date);

        patient = patientRepository.findPatientById(id).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

        patient.setAdmissionDate(date);

        baseRepository.detach(patient);
        patientRepository.save(patient);
    }

    public void changePatientUrgency(Long id, boolean urgent, String modifiedBy) {
        Patient patient = patientRepository.findPatientById(id).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));

        if (patient.getStatus().getName().equals(PatientStatusName.CONFIRMED_TWICE.name())
                && patient.getAdmissionDate().isBefore(LocalDate.now())) {
            throw new BadRequestException(ErrorKey.PATIENT_ALREADY_ADMITTED);
        }

        patient.setUrgent(urgent);
        patient.setModificationDate(Timestamp.from(Instant.now()));
        patient.setModifiedBy(accountRepository.findAccountByLogin(modifiedBy).orElseThrow(()
                -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND)));
        patient.setPatientType(patientTypeRepository.findPatientTypeByName(patient.findPatientType()).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_TYPE_NOT_FOUND)));
        baseRepository.detach(patient);
        patientRepository.save(patient);

        queueService.refreshQueueAfterUpdate(patient.getQueue());
    }

    public void deletePatient(Long id) {
        Patient patient = patientRepository.findPatientById(id).orElseThrow(()
                -> new NotFoundException(ErrorKey.PATIENT_NOT_FOUND));
        if (patient.getStatus().getName().equals(PatientStatusName.CONFIRMED_TWICE.name())) {
            throw new ConflictException(ErrorKey.PATIENT_CONFIRMED);
        }

        queueService.removePatientFromQueue(patient);

        patientRepository.delete(patient);
    }

    private boolean hasPermissionToCreateUrgentPatient(Account createdByAccount) {
        return createdByAccount.getAccessLevel().getName().equals(AccessLevelName.DOCTOR) ||
                createdByAccount.getAccessLevel().getName().equals(AccessLevelName.TREATMENT_DIRECTOR);
    }

    private void setMainDoctor(Patient patient, String mainDoctorLogin) {
        Account mainDoctor = accountRepository.findAccountByLogin(mainDoctorLogin).orElseThrow(()
                -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        if (!(mainDoctor.getAccessLevel().getName().equals(AccessLevelName.DOCTOR) ||
                mainDoctor.getAccessLevel().getName().equals(AccessLevelName.TREATMENT_DIRECTOR))) {
            throw new BadRequestException(ErrorKey.MAIN_DOCTOR_NOT_MEDIC);
        }
        patient.setMainDoctor(mainDoctor);
    }

    private List<Disease> getDiseasesFromDatabase(List<String> diseases) {
        return diseases.stream()
                .map(name -> diseaseRepository.findDiseaseByLatinName(name).orElseThrow(() ->
                        new NotFoundException(ErrorKey.DISEASE_NOT_FOUND)))
                .collect(Collectors.toList());
    }

    private void setPatientStatus(Patient patient) {
        if (patient.getStatus().getName().equals(PatientStatusName.WAITING.name())) {
            patient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_ONCE.name())
                    .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));
        } else {
            patient.setStatus(patientStatusRepository.findPatientStatusByName(PatientStatusName.CONFIRMED_TWICE.name())
                    .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_STATUS_NOT_FOUND)));
        }
    }

    private boolean checkIfDateIsWeekendOrFriday(LocalDate date) {
        return date.get(ChronoField.DAY_OF_WEEK) == 7 || date.get(ChronoField.DAY_OF_WEEK) == 6
                || date.get(ChronoField.DAY_OF_WEEK) == 5;
    }

    private boolean checkIfDateIsAtLeastTwoWeeksFromToday(LocalDate date) {
        return date.minusDays(14).isAfter(LocalDate.now()) || date.minusDays(14).isEqual(LocalDate.now());
    }

    private void setPatientFields(Patient patient, List<String> diseases, String mainDoctor, String covidStatus,
                                  Patient patientFromDB, String requestedBy) {

        patientFromDB.setVersion(patient.getVersion());
        if (patient.getPesel() != null && !patient.getPesel().isEmpty()) {
            patientFromDB.setPesel(patient.getPesel());
        }
        if (patient.getAge() != null && !patient.getAge().isEmpty()) {
            patientFromDB.setAge(patient.getAge());

        }
        if (patient.getSex() != null && (patient.getSex().equals("M") || patient.getSex().equals("F"))) {
            patientFromDB.setSex(patient.getSex());
        }
        if (diseases != null && !diseases.isEmpty()) {
            patientFromDB.setDiseases(getDiseasesFromDatabase(diseases));
        }
        if (mainDoctor != null && !mainDoctor.isEmpty()) {
            setMainDoctor(patientFromDB, mainDoctor);
        }
        if (covidStatus != null && !covidStatus.isEmpty()) {
            patientFromDB.setCovidStatus(covidStatusRepository.findCovidStatusByStatus(covidStatus).orElseThrow(()
                    -> new NotFoundException(ErrorKey.COVID_STATUS_NOT_FOUND)));
        }
        if (patient.getName() != null && !patient.getName().isEmpty()) {
            patientFromDB.setName(patient.getName());
        }
        if (patient.getSurname() != null && !patient.getSurname().isEmpty()) {
            patientFromDB.setSurname(patient.getSurname());
        }
        if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
            patientFromDB.setPhoneNumber(patient.getPhoneNumber());
        }
        if (patient.getEmailAddress() != null && !patient.getEmailAddress().isEmpty()) {
            patientFromDB.setEmailAddress(patient.getEmailAddress());
        }
        Account modifiedBy = accountRepository.findAccountByLogin(requestedBy).orElseThrow(()
                -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        patientFromDB.setModifiedBy(modifiedBy);
        patientFromDB.setModificationDate(Timestamp.from(Instant.now()));
        patientFromDB.setPatientType(patientTypeRepository.findPatientTypeByName(patientFromDB.findPatientType())
                .orElseThrow(() -> new NotFoundException(ErrorKey.PATIENT_TYPE_NOT_FOUND)));
    }
}
