package com.backend.hospitalward.util.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Aspect
@Component
public class AspectLogger {

    @Pointcut("execution(* com.backend.hospitalward.controller..*(..))")
    public void methodInController() {
    }

    @Pointcut("execution(* com.backend.hospitalward.mapper..*(..))")
    public void methodInMapper() {
    }

    @Pointcut("execution(* com.backend.hospitalward..*(..))")
    public void everyMethod() {
    }

    @Pointcut("execution(* com.backend.hospitalward.security.RequestJWTFilter.*(..))")
    public void methodInRequestFilter() {
    }

    @Around("everyMethod() && !methodInRequestFilter() && !methodInMapper() && !methodInController()")
    public Object advice(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();

        logEntry(joinPoint, method, methodName, className);

        return logExit(joinPoint, method);
    }

    @Around("methodInController()")
    public Object adviceController(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();

        logEntry(joinPoint, method, methodName, className);

        return logExitController(joinPoint, method);
    }

    @AfterThrowing(value = "everyMethod() && !methodInRequestFilter()", throwing = "e")
    public void logAfterThrowing(Exception e) {
        log.error(getExceptionMessage(e));
    }

    private Object logExitController(ProceedingJoinPoint joinPoint, Method method) throws Throwable {

        Instant start = Instant.now();
        Object response = joinPoint.proceed();
        Instant end = Instant.now();
        Long duration = Duration.between(start, end).toMillis();

        log.info(getExitControllerMessage(method.getDeclaringClass().getSimpleName(), method.getName(), duration, response));

        return response;
    }

    private String getExitControllerMessage(String className, String name, Long duration, Object response) {
        StringJoiner message = getBaseExitMessage(className, name, duration);

        Object body = ((ResponseEntity<?>) response).getBody();

        message.add("with HTTP status code:")
                .add(((ResponseEntity<?>) response).getStatusCode().name())
                .add("and body containing");

        if (body instanceof Collection) {
            message.add(Objects.requireNonNull(body).getClass().getSimpleName())
                    .add(", size:")
                    .add(String.valueOf(((Collection<?>) Objects.requireNonNull(body)).size()));
        } else if (body != null) {
            message.add(response.toString());
        } else {
            message.add("null");
        }

        return message.toString();
    }

    private void logEntry(ProceedingJoinPoint joinPoint, Method method, String methodName, String className) {
        Object[] args = joinPoint.getArgs();
        Parameter[] params = method.getParameters();

        log.info(getEntryMessage(className, methodName, params, args));
    }

    private Object logExit(ProceedingJoinPoint joinPoint, Method method) throws Throwable {
        String returnType = method.getReturnType().getSimpleName();

        Instant start = Instant.now();
        Object response = joinPoint.proceed();
        Instant end = Instant.now();
        Long duration = Duration.between(start, end).toMillis();

        log.info(getExitMessage(method.getDeclaringClass().getSimpleName(), method.getName(), duration, response,
                returnType));

        return response;
    }

    private StringJoiner getBaseExitMessage(String className, String name, Long duration) {
        return new StringJoiner(" ")
                .add("Class")
                .add(className)
                .add("finished method:")
                .add(name)
                .add("in")
                .add(duration.toString())
                .add("ms");
    }

    private String getExitMessage(String className, String name, Long duration, Object response, String returnType) {
        StringJoiner message = getBaseExitMessage(className, name, duration);

        if (returnType.equals("void")) {
            return message.toString();
        }

        message.add("and returned:");

        if (response instanceof Collection) {
            message.add(response.getClass().getSimpleName())
                    .add(", size:")
                    .add(String.valueOf(((Collection<?>) response).size()));
        } else if (response != null) {
            message.add(response.toString());
        } else {
            message.add("null");
        }

        return message.toString();
    }

    private String getEntryMessage(String className, String methodName, Parameter[] params, Object[] args) {

        StringJoiner message = new StringJoiner(" ")
                .add("Class")
                .add(className)
                .add("started method:")
                .add(methodName);

        if (Objects.nonNull(params) && Objects.nonNull(args) && params.length == args.length) {
            Map<String, Object> arguments = IntStream.range(0, params.length)
                    .boxed()
                    .collect(Collectors.toMap(
                            i -> params[i].getName(),
                            i -> args[i]
                    ));

            message.add("with args:")
                    .add(arguments.toString());
        }

        return message.toString();
    }

    private String getExceptionMessage(Exception e) {
        StringJoiner message = new StringJoiner(" ")
                .add(e.getClass().getSimpleName())
                .add("thrown in line:")
                .add(e.getStackTrace()[2].toString())
                .add("with message:")
                .add(e.getMessage());
        e.printStackTrace();

        return message.toString();
    }
}
