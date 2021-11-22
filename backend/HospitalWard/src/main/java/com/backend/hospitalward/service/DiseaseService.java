package com.backend.hospitalward.service;

import com.backend.hospitalward.model.Disease;
import com.backend.hospitalward.repository.DiseaseRepository;
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

    public List<Disease> getAllDiseases(){
        return diseaseRepository.findAll();
    }
}
