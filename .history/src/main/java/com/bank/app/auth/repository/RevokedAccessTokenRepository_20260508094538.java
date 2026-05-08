package com.bank.app.auth.repository;

import com.bank.app.auth.entity.RevokedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, Long> {
    Optional<RevokedAccessToken> findByToken(String token);
}
