package com.backend.hospitalward.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JWTUtils {

    private Boolean isTokenExpired(String token) {
        return extractExpirationTime(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpirationTime(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        String claim = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim(SecurityConstants.AUTH_CLAIM, claim)
                .setIssuer(SecurityConstants.ISSUER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + SecurityConstants.JWT_TIMEOUT))
                .signWith(SignatureAlgorithm.HS256, SecurityConstants.SECRET)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String login = extractUsername(token);
        return (login.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String refreshToken(String oldToken, String accessLevel) {
        Claims claims = Jwts.parser()
                .setSigningKey(SecurityConstants.SECRET)
                .requireIssuer(SecurityConstants.ISSUER)
                .parseClaimsJws(oldToken).getBody();

        return Jwts.builder()
                .setSubject(claims.getSubject())
                .claim("auth", accessLevel)
                .setIssuer(SecurityConstants.ISSUER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + SecurityConstants.JWT_TIMEOUT))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET)
                .compact();
    }

}
