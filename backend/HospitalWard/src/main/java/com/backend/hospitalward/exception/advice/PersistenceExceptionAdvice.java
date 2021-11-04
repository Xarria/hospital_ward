package com.backend.hospitalward.exception.advice;

import com.backend.hospitalward.dto.response.exception.ExceptionResponse;
import com.backend.hospitalward.exception.CommonException;
import com.backend.hospitalward.exception.ErrorKey;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PersistenceExceptionAdvice {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(OptimisticLockException.class)
    public void optimisticLockException(Exception e) {
        throw CommonException.createOptimisticLockException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(TransactionTimedOutException.class)
    public ExceptionResponse transactionTimedOutException(Exception e) {
        return ExceptionResponse.singleException(ErrorKey.TIMED_OUT, e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConstraintViolationException.class)
    public ExceptionResponse constraintViolationException(Exception e) {
        //TODO kilka naruszeń
        return ExceptionResponse.singleException(ErrorKey.CONSTRAINT_VIOLATION, e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(JDBCConnectionException.class)
    public ExceptionResponse jdbcConnectionException(Exception e) {
        return ExceptionResponse.singleException(ErrorKey.CONNECTION, e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(NoResultException.class)
    public ExceptionResponse noResultException(Exception e) {
        return ExceptionResponse.singleException(ErrorKey.NO_RESULT, e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(PersistenceException.class)
    public ExceptionResponse persistenceException(Exception e) {
        if (e.getCause() instanceof ConstraintViolationException) {
            constraintViolationException(e);
        }
        return ExceptionResponse.singleException(ErrorKey.PERSISTENCE, e.getMessage());
    }

}
