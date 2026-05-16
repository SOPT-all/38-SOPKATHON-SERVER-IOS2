package org.sopt.sopkathon.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtException;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-13T00:00:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("access token을 만들고 인증 객체로 복원한다")
    void createAccessTokenAndAuthenticate() {
        JwtTokenProvider tokenProvider = new JwtTokenProvider(
                new AppJwtProperties("test-issuer", SECRET, 2880),
                CLOCK
        );

        String token = tokenProvider.createAccessToken(1L, "ROLE_USER");
        Authentication authentication = tokenProvider.getAuthentication(token);

        assertThat(tokenProvider.getTokenType(token)).isEqualTo("access");
        assertThat(authentication.getName()).isEqualTo("1");
        assertThat(authentication.getPrincipal()).isEqualTo(new AuthenticatedUser(1L, "ROLE_USER"));
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("issuer가 다르면 인증 객체로 복원할 수 없다")
    void tokenWithWrongIssuer() {
        JwtTokenProvider issuingProvider = new JwtTokenProvider(
                new AppJwtProperties("issuer-a", SECRET, 2880),
                CLOCK
        );
        JwtTokenProvider validatingProvider = new JwtTokenProvider(
                new AppJwtProperties("issuer-b", SECRET, 2880),
                CLOCK
        );

        String token = issuingProvider.createAccessToken(1L, "ROLE_USER");

        assertThatThrownBy(() -> validatingProvider.getAuthentication(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 token은 인증 객체로 복원할 수 없다")
    void expiredToken() {
        AppJwtProperties properties = new AppJwtProperties("test-issuer", SECRET, 1);
        JwtTokenProvider issuingProvider = new JwtTokenProvider(
                properties,
                Clock.fixed(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC)
        );
        JwtTokenProvider validatingProvider = new JwtTokenProvider(
                properties,
                Clock.fixed(Instant.parse("2020-01-01T00:03:00Z"), ZoneOffset.UTC)
        );

        String token = issuingProvider.createAccessToken(1L, "ROLE_USER");

        assertThatThrownBy(() -> validatingProvider.getAuthentication(token))
                .isInstanceOf(JwtException.class);
    }
}
