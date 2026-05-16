package org.sopt.sopkathon.auth.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.auth.application.AuthService;
import org.sopt.sopkathon.auth.dto.LoginRequest;
import org.sopt.sopkathon.auth.dto.SignUpRequest;
import org.sopt.sopkathon.auth.dto.TokenResponse;
import org.sopt.sopkathon.global.error.GlobalExceptionHandler;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    @DisplayName("회원가입 API는 생성 상태와 토큰 응답을 반환한다")
    void signUp() throws Exception {
        TokenResponse tokenResponse = TokenResponse.bearer("access-token");
        given(authService.signUp(any(SignUpRequest.class))).willReturn(tokenResponse);

        SignUpRequest request = new SignUpRequest("student@sopt.org", "password123!", "해커톤");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_201"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("로그인 API는 공통 성공 응답으로 토큰을 반환한다")
    void login() throws Exception {
        TokenResponse tokenResponse = TokenResponse.bearer("access-token");
        given(authService.login(any(LoginRequest.class))).willReturn(tokenResponse);

        LoginRequest request = new LoginRequest("student@sopt.org", "password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("회원가입 API의 검증 실패는 공통 에러 응답으로 반환된다")
    void signUpValidationFailed() throws Exception {
        SignUpRequest request = new SignUpRequest("not-email", "short", "");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @DisplayName("회원가입 검증 실패는 비밀번호 rejectedValue를 노출하지 않는다")
    void signUpValidationFailedMasksPasswordRejectedValue() throws Exception {
        SignUpRequest request = new SignUpRequest("not-email", "short", "");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andReturn();

        JsonNode errors = objectMapper.readTree(result.getResponse().getContentAsString()).get("errors");
        JsonNode emailError = fieldError(errors, "email");
        JsonNode passwordError = fieldError(errors, "password");

        assertThat(emailError.get("rejectedValue").asText()).isEqualTo("not-email");
        assertThat(passwordError.get("rejectedValue").asText()).isEqualTo("[MASKED]");
    }

    private JsonNode fieldError(JsonNode errors, String field) {
        return StreamSupport.stream(errors.spliterator(), false)
                .filter(error -> field.equals(error.get("field").asText()))
                .findFirst()
                .orElseThrow();
    }
}
