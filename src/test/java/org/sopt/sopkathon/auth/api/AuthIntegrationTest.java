package org.sopt.sopkathon.auth.api;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.support.IntegrationTestSupport;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("실제 MySQL 컨테이너에서 회원가입 후 로그인할 수 있다")
    void signUpAndLoginWithRealMySql() throws Exception {
        String signUpJson = """
                {
                  "email": "integration@sopt.org",
                  "password": "password123!",
                  "name": "Integration"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());

        String loginJson = """
                {
                  "email": "integration@sopt.org",
                  "password": "password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }
}
