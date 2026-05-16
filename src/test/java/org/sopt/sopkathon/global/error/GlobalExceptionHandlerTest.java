package org.sopt.sopkathon.global.error;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    @DisplayName("비즈니스 예외는 ErrorCode 기반 응답과 traceId를 반환한다")
    void businessException() throws Exception {
        mockMvc.perform(get("/test/business-error")
                        .header(TraceIdFilter.TRACE_ID_HEADER, "trace-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON_400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.path").value("/test/business-error"))
                .andExpect(jsonPath("$.traceId").value("trace-123"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("검증 예외는 필드별 오류 목록을 반환한다")
    void validationException() throws Exception {
        TestRequest request = new TestRequest("");

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("이름은 필수입니다."));
    }

    @Test
    @DisplayName("필수 query parameter가 없으면 공통 검증 실패 응답을 반환한다")
    void missingRequestParameter() throws Exception {
        mockMvc.perform(get("/test/query-required"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/test/query-required"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("keyword"));
    }

    @Test
    @DisplayName("path variable 타입이 맞지 않으면 공통 검증 실패 응답을 반환한다")
    void pathVariableTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/members/not-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/test/members/not-number"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("memberId"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("not-number"));
    }

    @Test
    @DisplayName("query parameter 제약 조건을 어기면 공통 검증 실패 응답을 반환한다")
    void requestParameterConstraintViolation() throws Exception {
        mockMvc.perform(get("/test/paging")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/test/paging"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("page"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("0"))
                .andExpect(jsonPath("$.errors[0].message").value("페이지는 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("처리하지 못한 예외는 내부 오류 응답으로 감싸고 구현 상세를 숨긴다")
    void unexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("COMMON_500"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/business-error")
        void businessError() {
            throw new BusinessException(CommonErrorCode.COMMON_BAD_REQUEST);
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/query-required")
        void queryRequired(@RequestParam String keyword) {
        }

        @GetMapping("/members/{memberId}")
        void member(@PathVariable Long memberId) {
        }

        @GetMapping("/paging")
        void paging(@RequestParam @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") int page) {
        }

        @GetMapping("/unexpected-error")
        void unexpectedError() {
            throw new IllegalStateException("database password leaked");
        }
    }

    record TestRequest(@NotBlank(message = "이름은 필수입니다.") String name) {
    }
}
