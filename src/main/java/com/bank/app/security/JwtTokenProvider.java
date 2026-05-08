package com.bank.app.security;

import com.bank.app.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecurityProperties securityProperties;

    public JwtTokenProvider(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(securityProperties.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(securityProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("roles", user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(securityProperties.refreshTokenExpirationDays(), ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(securityProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("typ", "refresh")
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public long accessTokenExpirationSeconds() {
        return securityProperties.accessTokenExpirationMinutes() * 60;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(securityProperties.secret().getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
