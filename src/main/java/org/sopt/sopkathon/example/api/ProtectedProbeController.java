package org.sopt.sopkathon.example.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.sopt.sopkathon.example.dto.ExampleResponse;
import org.sopt.sopkathon.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 인증이 필요한 API 예시다.
// 실제 기능 API가 아니라 JWT 설정, Swagger 자물쇠 표시, smoke test를 빠르게 확인하는 probe다.
@RestController
public class ProtectedProbeController {

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/protected")
    public ApiResponse<ExampleResponse> protectedProbe() {
        return ApiResponse.success(new ExampleResponse("ok", "Protected API is available."));
    }
}
