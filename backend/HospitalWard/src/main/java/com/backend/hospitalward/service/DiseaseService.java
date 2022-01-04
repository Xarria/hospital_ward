package com.backend.hospitalward.service;

import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.Disease;
import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.model.Queue;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.BaseRepository;
import com.backend.hospitalward.repository.DiseaseRepository;
import com.backend.hospitalward.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Retryable(value = {PersistenceException.class, HibernateException.class, JDBCException.class},
        exclude = ConstraintViolationException.class, backoff = @Backoff(delay = 1000))
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class DiseaseService {

    DiseaseRepository diseaseRepository;

    AccountRepository accountRepository;

    BaseRepository baseRepository;

    QueueService queueService;

    PatientRepository patientRepository;

    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAll();
    }

    public Disease getDiseaseByName(String name) {
        return diseaseRepository.findDiseaseByLatinName(name).orElseThrow(()
                -> new NotFoundException(ErrorKey.DISEASE_NOT_FOUND));
    }

    public void createDisease(Disease disease, String createdBy) {
        Account account = accountRepository.findAccountByLogin(createdBy).orElseThrow(()
                -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        disease.setVersion(0L);
        disease.setPatients(Collections.emptyList());
        disease.setCreatedBy(account);
        disease.setCreationDate(Timestamp.from(Instant.now()));
        diseaseRepository.save(disease);
    }

    public void updateDisease(Disease disease, String name, String modifiedBy) {
        Disease diseaseFromDB = diseaseRepository.findDiseaseByLatinName(name).orElseThrow(() ->
                new NotFoundException(ErrorKey.DISEASE_NOT_FOUND));

        baseRepository.detach(diseaseFromDB);

        diseaseFromDB.setVersion(disease.getVersion());

        diseaseFromDB.setCathererRequired(disease.isCathererRequired());
        diseaseFromDB.setSurgeryRequired(disease.isSurgeryRequired());
        diseaseFromDB.setModificationDate(Timestamp.from(Instant.now()));

        Account accModifiedBy = accountRepository.findAccountByLogin(modifiedBy).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        diseaseFromDB.setModifiedBy(accModifiedBy);

        diseaseRepository.save(diseaseFromDB);

        if (patientRepository.findAll().stream().anyMatch(p -> p.getDiseases().contains(diseaseFromDB)
                && p.getQueue().getDate().isAfter(LocalDate.now()))) {
            List<Queue> queuesToRefresh = patientRepository.findAll().stream()
                    .filter(p -> p.getDiseases().contains(diseaseFromDB) && p.getQueue().getDate().isAfter(LocalDate.now()))
                    .map(Patient::getQueue).distinct()
                    .collect(Collectors.toList());
            queuesToRefresh.forEach(queueService::refreshQueueAfterDiseaseUpdate);
        }
    }

    public void deleteDisease(String name) {
        Disease disease = diseaseRepository.findDiseaseByLatinName(name).orElseThrow(() ->
                new NotFoundException(ErrorKey.DISEASE_NOT_FOUND));

        if (disease.getPatients() != null && !disease.getPatients().isEmpty()) {
            throw new ConflictException(ErrorKey.DISEASE_ASSIGNED_TO_PATIENT);
        }

        diseaseRepository.delete(disease);
    }
}
