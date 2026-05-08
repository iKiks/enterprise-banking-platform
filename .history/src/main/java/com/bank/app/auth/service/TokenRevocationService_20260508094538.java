package com.bank.app.auth.service;

import com.bank.app.auth.entity.RevokedAccessToken;
import com.bank.app.auth.repository.RevokedAccessTokenRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

@Service
public class TokenRevocationService {

    private final RevokedAccessTokenRepository revokedAccessTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public TokenRevocationService(RevokedAccessTokenRepository revokedAccessTokenRepository,
                                  RedisTemplate<String, String> redisTemplate) {
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
        this.redisTemplate = redisTemplate;
    }

    public void revokeAccessToken(String token, LocalDateTime expiresAt) {
        // prefer Redis blacklist with TTL for performance
        try {
            long seconds = Math.max(60, Duration.between(LocalDateTime.now(), expiresAt).getSeconds());
            redisTemplate.opsForValue().set("revoked:access:" + token, "1", seconds, TimeUnit.SECONDS);
        } catch (Exception ex) {
            // fallback to DB storage
            RevokedAccessToken t = new RevokedAccessToken();
            t.setToken(token);
            t.setExpiresAt(expiresAt);
            revokedAccessTokenRepository.save(t);
        }
    }

    public boolean isRevoked(String token) {
        try {
            String key = redisTemplate.opsForValue().get("revoked:access:" + token);
            if (key != null) return true;
        } catch (Exception ignored) {
        }
        return revokedAccessTokenRepository.findByToken(token).isPresent();
    }
}
