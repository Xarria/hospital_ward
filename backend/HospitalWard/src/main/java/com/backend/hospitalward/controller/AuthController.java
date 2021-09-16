package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.Credentials;
import com.backend.hospitalward.security.JWTUtils;
import com.backend.hospitalward.security.SecurityConstants;
import com.backend.hospitalward.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final AccountService accountService;

    private final JWTUtils jwtUtils;


    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody Credentials credentials) {
        try {
            //credentials.setPassword(Sha512DigestUtils.shaHex(credentials.getPassword()));
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getLogin(), credentials.getPassword()));
        } catch (BadCredentialsException | LockedException | DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        UserDetails userDetails = accountService.loadUserByUsername(credentials.getLogin());

        return ResponseEntity.ok(jwtUtils.generateToken(userDetails));
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CurrentSecurityContext HttpServletRequest servletRequest){
        String authHeader = servletRequest.getHeader(SecurityConstants.AUTHORIZATION);
        String oldJwt = authHeader.substring(SecurityConstants.BEARER.length()).trim();

        return ResponseEntity.ok(jwtUtils.refreshToken(oldJwt));
    }

}
