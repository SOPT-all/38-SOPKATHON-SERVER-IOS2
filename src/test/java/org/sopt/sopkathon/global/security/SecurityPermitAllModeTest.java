package org.sopt.sopkathon.global.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.security.permit-all=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityPermitAllModeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberRepository memberRepository;

    @Test
    @DisplayName("permit-all 모드에서는 새 API가 인증 필터에 막히지 않고 MVC 404까지 도달한다")
    void permitAllModeDoesNotBlockUnknownEndpointWith401() throws Exception {
        mockMvc.perform(get("/api/v1/new-feature-before-auth"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMON_404"));
    }

    @Test
    @DisplayName("permit-all 모드에서는 보호된 probe API가 성공 응답을 반환한다")
    void protectedProbeEndpointInPermitAllMode() throws Exception {
        mockMvc.perform(get("/api/v1/protected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.data.status").value("ok"));
    }
}
