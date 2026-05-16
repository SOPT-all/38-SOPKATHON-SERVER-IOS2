package org.sopt.sopkathon.global.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.sopt.sopkathon.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Swagger, actuator health, example health는 인증 없이 접근할 수 있다")
    void publicEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/examples/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Swagger에서 보호 API는 bearer 인증 필요 여부를 표시한다")
    void protectedEndpointHasBearerAuthInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/protected'].get.security[0].bearerAuth").isArray());
    }

    @Test
    @DisplayName("보호된 API를 token 없이 호출하면 공통 401 응답을 반환한다")
    void protectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_401"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("보호된 API를 유효한 token으로 호출하면 공통 성공 응답을 반환한다")
    void protectedEndpointWithValidToken() throws Exception {
        String token = jwtTokenProvider.createAccessToken(1L, "ROLE_USER");

        mockMvc.perform(get("/api/v1/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.data.status").value("ok"));
    }

    @Test
    @DisplayName("관리자 API를 USER token으로 호출하면 공통 403 응답을 반환한다")
    void adminEndpointWithUserToken() throws Exception {
        String token = jwtTokenProvider.createAccessToken(1L, "ROLE_USER");

        mockMvc.perform(get("/api/v1/admin/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_403"))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
