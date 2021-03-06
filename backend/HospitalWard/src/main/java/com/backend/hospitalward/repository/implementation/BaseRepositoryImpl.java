package com.backend.hospitalward.repository.implementation;

import com.backend.hospitalward.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

@Repository
public class BaseRepositoryImpl implements BaseRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void detach(Object obj) {
        entityManager.detach(obj);
    }

    @Override
    public void pessimisticLock(Object obj) {
        entityManager.lock(obj, LockModeType.PESSIMISTIC_WRITE);
    }
}
