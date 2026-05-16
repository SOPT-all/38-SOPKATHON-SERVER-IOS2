package org.sopt.sopkathon.auth.application;

import org.sopt.sopkathon.auth.dto.LoginRequest;
import org.sopt.sopkathon.auth.dto.SignUpRequest;
import org.sopt.sopkathon.auth.dto.TokenResponse;
import org.sopt.sopkathon.auth.error.AuthErrorCode;
import org.sopt.sopkathon.global.error.BusinessException;
import org.sopt.sopkathon.global.security.JwtTokenProvider;
import org.sopt.sopkathon.member.domain.Member;
import org.sopt.sopkathon.member.domain.Role;
import org.sopt.sopkathon.member.error.MemberErrorCode;
import org.sopt.sopkathon.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public TokenResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(MemberErrorCode.MEMBER_DUPLICATE_EMAIL);
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
                .orElseThrow(() -> new BusinessException(AuthErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.password())) {
            throw new BusinessException(AuthErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        return issueTokens(member);
    }

    private TokenResponse issueTokens(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.id(), member.role().name());
        return TokenResponse.bearer(accessToken);
    }
}
