package com.backend.hospitalward.repository;

public interface BaseRepository {

    void detach(Object obj);

    void pessimisticLock(Object obj);
}
