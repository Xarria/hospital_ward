package com.backend.hospitalward.exception.advice;

import com.backend.hospitalward.dto.response.exception.ExceptionResponse;
import com.backend.hospitalward.exception.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationExceptionAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ExceptionResponse notFoundException(NotFoundException e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ExceptionResponse conflictException(ConflictException e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ExceptionResponse badRequestException(BadRequestException e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ExceptionResponse exception(ConstraintViolationException e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(GoneException.class)
    public ExceptionResponse goneException(GoneException e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ExceptionHandler(PreconditionFailedException.class)
    public ExceptionResponse preconditionFailedException(PreconditionFailedException e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ExceptionResponse unauthorizedException(UnauthorizedException e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UnknownException.class)
    public ExceptionResponse unknownException(UnknownException e) {
        return ExceptionResponse.singleException(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ExceptionResponse forbiddenException(AccessDeniedException e) {
        return ExceptionResponse.singleException(ErrorKey.ACCESS_DENIED);
    }

}
