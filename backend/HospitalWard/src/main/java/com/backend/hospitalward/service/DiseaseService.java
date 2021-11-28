package com.backend.hospitalward.service;

import com.backend.hospitalward.exception.ConflictException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.Disease;
import com.backend.hospitalward.model.DiseaseUrgency;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.DiseaseRepository;
import com.backend.hospitalward.repository.DiseaseUrgencyRepository;
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
import java.util.Collections;
import java.util.List;

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

    DiseaseUrgencyRepository diseaseUrgencyRepository;

    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAll();
    }

    public Disease getDiseaseByName(String name) {
        return diseaseRepository.findDiseaseByName(name).orElseThrow(()
                -> new NotFoundException(ErrorKey.DISEASE_NOT_FOUND));
    }

    public void createDisease(Disease disease, String login, String urgency) {
        Account account = accountRepository.findAccountByLogin(login).orElseThrow(()
                -> new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        DiseaseUrgency diseaseUrgency = diseaseUrgencyRepository.findDiseaseUrgencyByUrgency(urgency).orElseThrow(()
                -> new NotFoundException(ErrorKey.DISEASE_URGENCY_NOT_FOUND));
        disease.setVersion(0L);
        disease.setPatients(Collections.emptyList());
        disease.setUrgency(diseaseUrgency);
        disease.setCreatedBy(account);
        disease.setCreationDate(Timestamp.from(Instant.now()));
        diseaseRepository.save(disease);
    }

    public void updateDisease(Disease disease, String modifiedBy, String urgency) {
        Disease diseaseFromDB = diseaseRepository.findDiseaseByName(disease.getName()).orElseThrow(() ->
                new NotFoundException(ErrorKey.DISEASE_NOT_FOUND));
        if (urgency != null && !urgency.isEmpty()) {
            DiseaseUrgency diseaseUrgency = diseaseUrgencyRepository.findDiseaseUrgencyByUrgency(urgency).orElseThrow(()
                    -> new NotFoundException(ErrorKey.DISEASE_URGENCY_NOT_FOUND));
            diseaseFromDB.setUrgency(diseaseUrgency);
        }

        diseaseFromDB.setVersion(disease.getVersion());

        if (disease.getName() != null && !disease.getName().isEmpty()) {
            diseaseFromDB.setName(disease.getName());
        }
        diseaseFromDB.setCathererRequired(disease.isCathererRequired());
        diseaseFromDB.setSurgeryRequired(disease.isSurgeryRequired());
        diseaseFromDB.setModificationDate(Timestamp.from(Instant.now()));

        Account accModifiedBy = accountRepository.findAccountByLogin(modifiedBy).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
        diseaseFromDB.setModifiedBy(accModifiedBy);

        diseaseRepository.save(diseaseFromDB);
    }

    public void deleteDisease(String name) {
        Disease disease = diseaseRepository.findDiseaseByName(name).orElseThrow(() ->
                new NotFoundException(ErrorKey.DISEASE_NOT_FOUND));

        if (disease.getPatients() != null && !disease.getPatients().isEmpty()) {
            throw new ConflictException(ErrorKey.DISEASE_ASSIGNED_TO_PATIENT);
        }

        diseaseRepository.delete(disease);
    }
}
