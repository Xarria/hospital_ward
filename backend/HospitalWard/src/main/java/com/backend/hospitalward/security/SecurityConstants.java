package com.backend.hospitalward.security;

public class SecurityConstants {

    public static final String ADMIN = "ADMIN";
    public static final String DOCTOR = "DOCTOR";
    public static final String HEAD_NURSE = "HEAD_NURSE";
    public static final String SECRET = "";
    public static final long JWT_TIMEOUT = 15 * 60 * 1000;
    public static final String ISSUER = "HospitalWard";
    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";
}
