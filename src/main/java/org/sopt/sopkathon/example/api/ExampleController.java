package org.sopt.sopkathon.example.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.sopkathon.example.dto.ExampleRequest;
import org.sopt.sopkathon.example.dto.ExampleResponse;
import org.sopt.sopkathon.global.error.BusinessException;
import org.sopt.sopkathon.global.error.ErrorCode;
import org.sopt.sopkathon.global.error.GlobalErrorResponse;
import org.sopt.sopkathon.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Example", description = "공통 응답/검증/에러 포맷 확인용 샘플 API")
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleController {

    @Operation(summary = "샘플 health API", description = "공통 성공 응답 포맷을 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "샘플 API 사용 가능")
    })
    @GetMapping("/health")
    public ApiResponse<ExampleResponse> health() {
        return ApiResponse.success(new ExampleResponse("ok", "Example API is available."));
    }

    @Operation(summary = "샘플 생성 API", description = "공통 생성 응답과 validation 실패 포맷을 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "샘플 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ExampleResponse>> create(@Valid @RequestBody ExampleRequest request) {
        ExampleResponse response = new ExampleResponse("created", "Hello, " + request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "샘플 비즈니스 에러 API", description = "공통 비즈니스 예외 응답 포맷을 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "샘플 비즈니스 예외",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            )
    })
    @GetMapping("/error")
    public void businessError() {
        throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST);
    }
}
