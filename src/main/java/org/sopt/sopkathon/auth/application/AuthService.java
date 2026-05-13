package org.sopt.sopkathon.auth.application;

import java.time.Clock;
import java.time.Instant;
import org.sopt.sopkathon.auth.domain.RefreshToken;
import org.sopt.sopkathon.auth.dto.LoginRequest;
import org.sopt.sopkathon.auth.dto.RefreshTokenRequest;
import org.sopt.sopkathon.auth.dto.SignUpRequest;
import org.sopt.sopkathon.auth.dto.TokenResponse;
import org.sopt.sopkathon.auth.repository.RefreshTokenRepository;
import org.sopt.sopkathon.global.error.BusinessException;
import org.sopt.sopkathon.global.error.ErrorCode;
import org.sopt.sopkathon.global.security.JwtTokenProvider;
import org.sopt.sopkathon.member.domain.Member;
import org.sopt.sopkathon.member.domain.Role;
import org.sopt.sopkathon.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;
    private final Clock clock;

    public AuthService(
            MemberRepository memberRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenHasher refreshTokenHasher,
            Clock clock
    ) {
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenHasher = refreshTokenHasher;
        this.clock = clock;
    }

    @Transactional
    public TokenResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.MEMBER_DUPLICATE_EMAIL);
        }

        Member member = Member.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                Role.ROLE_USER
        );
        Member savedMember = memberRepository.save(member);
        return issueTokens(savedMember);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.password())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        refreshTokenRepository.deleteByMemberId(member.id());
        return issueTokens(member);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String tokenType = jwtTokenProvider.getTokenType(request.refreshToken());
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        String oldTokenHash = refreshTokenHasher.hash(request.refreshToken());
        RefreshToken storedRefreshToken = refreshTokenRepository.findByTokenHash(oldTokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_TOKEN));

        if (storedRefreshToken.expiresAt().isBefore(Instant.now(clock))) {
            refreshTokenRepository.deleteByTokenHash(oldTokenHash);
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        Long memberId = Long.valueOf(jwtTokenProvider.getSubject(request.refreshToken()));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        refreshTokenRepository.deleteByTokenHash(oldTokenHash);
        return issueTokens(member);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenHash = refreshTokenHasher.hash(request.refreshToken());
        refreshTokenRepository.deleteByTokenHash(tokenHash);
    }

    private TokenResponse issueTokens(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.id(), member.role().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.id());
        String tokenHash = refreshTokenHasher.hash(refreshToken);
        refreshTokenRepository.save(RefreshToken.create(member.id(), tokenHash, jwtTokenProvider.getExpiresAt(refreshToken)));
        return TokenResponse.bearer(accessToken, refreshToken);
    }
}
