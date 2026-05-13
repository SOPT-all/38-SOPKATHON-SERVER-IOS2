package org.sopt.sopkathon.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record AppJwtProperties(
        String issuer,
        String secret,
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {
}
