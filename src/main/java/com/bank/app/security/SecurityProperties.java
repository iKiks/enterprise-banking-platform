package com.bank.app.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record SecurityProperties(
        String issuer,
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays,
        String secret
) {
}
