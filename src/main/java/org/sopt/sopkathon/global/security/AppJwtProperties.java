package org.sopt.sopkathon.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record AppJwtProperties(
        // issuer는 토큰을 발급한 서버 이름이다. prod에서도 클라이언트에 노출되어도 되는 값만 쓴다.
        String issuer,
        // secret은 토큰 서명 키다. prod에서는 반드시 긴 랜덤 문자열을 환경변수로 넣는다.
        String secret,
        long accessTokenExpirationMinutes
) {
}
