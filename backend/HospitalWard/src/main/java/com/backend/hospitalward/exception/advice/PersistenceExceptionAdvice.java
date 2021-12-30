package com.backend.hospitalward.exception.advice;

import com.backend.hospitalward.dto.response.exception.ExceptionResponse;
import org.hibernate.TransactionException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ExceptionResponse optimisticLockException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(TransactionException.class)
    public ExceptionResponse transactionTimedOutException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ExceptionResponse constraintViolationException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ExceptionResponse dataIntegrityException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(JDBCConnectionException.class)
    public ExceptionResponse jdbcConnectionException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResultException.class)
    public ExceptionResponse noResultException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PersistenceException.class)
    public ExceptionResponse persistenceException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

}
