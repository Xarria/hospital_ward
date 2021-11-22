package com.backend.hospitalward.exception.advice;

import com.backend.hospitalward.dto.response.exception.ExceptionResponse;
import org.hibernate.TransactionException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
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

    //TODO statusy
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(OptimisticLockException.class)
    public ExceptionResponse optimisticLockException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(TransactionException.class)
    public ExceptionResponse transactionTimedOutException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConstraintViolationException.class)
    public ExceptionResponse constraintViolationException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(JDBCConnectionException.class)
    public ExceptionResponse jdbcConnectionException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(NoResultException.class)
    public ExceptionResponse noResultException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(PersistenceException.class)
    public ExceptionResponse persistenceException(Exception e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

}
