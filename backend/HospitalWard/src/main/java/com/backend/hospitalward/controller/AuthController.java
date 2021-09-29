package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.Credentials;
import com.backend.hospitalward.security.JWTUtils;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class AuthController {

    AuthenticationManager authenticationManager;

    AuthService authService;

    JWTUtils jwtUtils;


    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody Credentials credentials) {

        try {
            credentials.setPassword(Sha512DigestUtils.shaHex(credentials.getPassword()));
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getLogin(), credentials.getPassword()));
        } catch (BadCredentialsException | LockedException | DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        UserDetails userDetails = authService.loadUserByUsername(credentials.getLogin());

        return ResponseEntity.ok(jwtUtils.generateToken(userDetails));
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CurrentSecurityContext HttpServletRequest servletRequest) {
        String accessLevel = authService.findAccessLevelByLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        String authHeader = servletRequest.getHeader(SecurityConstants.AUTHORIZATION);
        String oldJwt = authHeader.substring(SecurityConstants.BEARER.length()).trim();

        return ResponseEntity.ok(jwtUtils.refreshToken(oldJwt, accessLevel));
    }

}
