package org.sopt.sopkathon.example.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.example.dto.ExampleRequest;
import org.sopt.sopkathon.global.error.GlobalExceptionHandler;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class ExampleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ExampleController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    @DisplayName("샘플 health API는 공통 성공 응답 포맷을 보여준다")
    void health() throws Exception {
        mockMvc.perform(get("/api/v1/examples/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.message").value("요청이 성공했습니다."))
                .andExpect(jsonPath("$.data.status").value("ok"))
                .andExpect(jsonPath("$.data.description").value("Example API is available."));
    }

    @Test
    @DisplayName("샘플 생성 API는 공통 생성 응답 포맷을 보여준다")
    void create() throws Exception {
        ExampleRequest request = new ExampleRequest("SOPT");

        mockMvc.perform(post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_201"))
                .andExpect(jsonPath("$.data.status").value("created"))
                .andExpect(jsonPath("$.data.description").value("Hello, SOPT"));
    }

    @Test
    @DisplayName("샘플 생성 API의 검증 실패는 공통 에러 응답 포맷을 보여준다")
    void createValidationFailed() throws Exception {
        ExampleRequest request = new ExampleRequest("");

        mockMvc.perform(post("/api/v1/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    @DisplayName("샘플 에러 API는 비즈니스 예외 응답 포맷을 보여준다")
    void businessError() throws Exception {
        mockMvc.perform(get("/api/v1/examples/error")
                        .header(TraceIdFilter.TRACE_ID_HEADER, "example-trace"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400"))
                .andExpect(jsonPath("$.traceId").value("example-trace"));
    }
}
