package org.sopt.sopkathon.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.auth.domain.RefreshToken;
import org.sopt.sopkathon.auth.dto.LoginRequest;
import org.sopt.sopkathon.auth.dto.RefreshTokenRequest;
import org.sopt.sopkathon.auth.dto.SignUpRequest;
import org.sopt.sopkathon.auth.dto.TokenResponse;
import org.sopt.sopkathon.auth.repository.RefreshTokenRepository;
import org.sopt.sopkathon.global.error.BusinessException;
import org.sopt.sopkathon.global.error.ErrorCode;
import org.sopt.sopkathon.global.security.AppJwtProperties;
import org.sopt.sopkathon.global.security.JwtTokenProvider;
import org.sopt.sopkathon.member.domain.Member;
import org.sopt.sopkathon.member.domain.Role;
import org.sopt.sopkathon.member.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-13T00:00:00Z"), ZoneOffset.UTC);
    private static final String SECRET = "auth-service-test-secret-key-that-is-long-enough";

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenHasher refreshTokenHasher;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtTokenProvider = new JwtTokenProvider(new AppJwtProperties("test-issuer", SECRET, 60, 14), CLOCK);
        refreshTokenHasher = new RefreshTokenHasher();
        authService = new AuthService(
                memberRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenHasher,
                CLOCK
        );
    }

    @Test
    @DisplayName("회원가입은 비밀번호를 암호화하고 access/refresh token을 발급한다")
    void signUp() {
        SignUpRequest request = new SignUpRequest("student@sopt.org", "password123!", "해커톤");
        given(memberRepository.existsByEmail(request.email())).willReturn(false);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.assignIdForTest(1L);
            return member;
        });

        TokenResponse response = authService.signUp(request);

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("이미 존재하는 email로 회원가입하면 비즈니스 예외가 발생한다")
    void signUpDuplicateEmail() {
        SignUpRequest request = new SignUpRequest("student@sopt.org", "password123!", "해커톤");
        given(memberRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.MEMBER_DUPLICATE_EMAIL));
    }

    @Test
    @DisplayName("로그인은 비밀번호를 검증하고 token을 발급한다")
    void login() {
        String encodedPassword = passwordEncoder.encode("password123!");
        Member member = Member.create("student@sopt.org", encodedPassword, "해커톤", Role.ROLE_USER);
        member.assignIdForTest(1L);
        given(memberRepository.findByEmail("student@sopt.org")).willReturn(Optional.of(member));

        TokenResponse response = authService.login(new LoginRequest("student@sopt.org", "password123!"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 비밀번호가 틀리면 인증 예외가 발생한다")
    void loginWrongPassword() {
        String encodedPassword = passwordEncoder.encode("password123!");
        Member member = Member.create("student@sopt.org", encodedPassword, "해커톤", Role.ROLE_USER);
        member.assignIdForTest(1L);
        given(memberRepository.findByEmail("student@sopt.org")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.login(new LoginRequest("student@sopt.org", "wrong-password")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("refresh는 저장된 refresh token hash를 확인하고 token을 회전한다")
    void refresh() {
        Member member = Member.create("student@sopt.org", passwordEncoder.encode("password123!"), "해커톤", Role.ROLE_USER);
        member.assignIdForTest(1L);
        String refreshToken = jwtTokenProvider.createRefreshToken(1L);
        String tokenHash = refreshTokenHasher.hash(refreshToken);
        given(refreshTokenRepository.findByTokenHash(tokenHash))
                .willReturn(Optional.of(RefreshToken.create(1L, tokenHash, Instant.parse("2026-05-27T00:00:00Z"))));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        TokenResponse response = authService.refresh(new RefreshTokenRequest(refreshToken));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotEqualTo(refreshToken);
        verify(refreshTokenRepository).deleteByTokenHash(tokenHash);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("refresh token이 만료되면 저장된 token hash를 삭제하고 인증 예외를 발생시킨다")
    void refreshExpiredToken() {
        String refreshToken = jwtTokenProvider.createRefreshToken(1L);
        String tokenHash = refreshTokenHasher.hash(refreshToken);
        given(refreshTokenRepository.findByTokenHash(tokenHash))
                .willReturn(Optional.of(RefreshToken.create(1L, tokenHash, CLOCK.instant().minusSeconds(1))));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(refreshToken)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.AUTH_INVALID_TOKEN));

        verify(refreshTokenRepository).deleteByTokenHash(tokenHash);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("access token으로 refresh를 요청하면 인증 예외가 발생한다")
    void refreshWithAccessToken() {
        String accessToken = jwtTokenProvider.createAccessToken(1L, Role.ROLE_USER.name());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(accessToken)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.AUTH_INVALID_TOKEN));
    }

    @Test
    @DisplayName("로그아웃은 refresh token hash를 삭제한다")
    void logout() {
        String refreshToken = jwtTokenProvider.createRefreshToken(1L);
        String tokenHash = refreshTokenHasher.hash(refreshToken);

        authService.logout(new RefreshTokenRequest(refreshToken));

        verify(refreshTokenRepository).deleteByTokenHash(tokenHash);
    }
}
