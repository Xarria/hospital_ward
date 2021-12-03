package com.backend.hospitalward.security;

import com.backend.hospitalward.dto.request.auth.Credentials;
import com.backend.hospitalward.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class RequestJWTFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;

    private final AuthService authService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION);

        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER)) {
            jwt = authHeader.substring(SecurityConstants.BEARER.length()).trim();
            try {
                username = jwtUtils.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                log.error("JWT token expired");
            } catch (Exception e) {
                log.error("Invalid JWT Token");
            }
        } else {
            log.warn("Invalid authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = authService.loadUserByUsername(username);

            if (jwtUtils.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                new Credentials(userDetails.getUsername(), userDetails.getPassword()),
                                userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
