package org.sopt.sopkathon.auth.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.auth.application.AuthService;
import org.sopt.sopkathon.auth.dto.LoginRequest;
import org.sopt.sopkathon.auth.dto.RefreshTokenRequest;
import org.sopt.sopkathon.auth.dto.SignUpRequest;
import org.sopt.sopkathon.auth.dto.TokenResponse;
import org.sopt.sopkathon.global.error.GlobalExceptionHandler;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
        TokenResponse tokenResponse = TokenResponse.bearer("access-token", "refresh-token");
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
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("로그인 API는 공통 성공 응답으로 토큰을 반환한다")
    void login() throws Exception {
        TokenResponse tokenResponse = TokenResponse.bearer("access-token", "refresh-token");
        given(authService.login(any(LoginRequest.class))).willReturn(tokenResponse);

        LoginRequest request = new LoginRequest("student@sopt.org", "password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("토큰 재발급 API는 refresh token 요청을 받아 회전된 토큰을 반환한다")
    void refresh() throws Exception {
        TokenResponse tokenResponse = TokenResponse.bearer("new-access-token", "new-refresh-token");
        given(authService.refresh(any(RefreshTokenRequest.class))).willReturn(tokenResponse);

        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("로그아웃 API는 refresh token을 폐기하고 빈 성공 응답을 반환한다")
    void logout() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_204"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(authService).logout(any(RefreshTokenRequest.class));
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
}
