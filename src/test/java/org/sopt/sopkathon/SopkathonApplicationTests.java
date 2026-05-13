package org.sopt.sopkathon;

import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.auth.repository.RefreshTokenRepository;
import org.sopt.sopkathon.member.repository.MemberRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class SopkathonApplicationTests {

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void contextLoads() {
    }

}
