package org.sopt.sopkathon.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.sopkathon.auth.application.AuthService;
import org.sopt.sopkathon.auth.dto.LoginRequest;
import org.sopt.sopkathon.auth.dto.SignUpRequest;
import org.sopt.sopkathon.auth.dto.TokenResponse;
import org.sopt.sopkathon.global.error.GlobalErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.sopt.sopkathon.global.response.ApiResponse.created;
import static org.sopt.sopkathon.global.response.ApiResponse.success;

@Tag(name = "Auth", description = "자체 로그인/JWT 인증 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "회원가입 후 48시간짜리 access token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 이메일",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<org.sopt.sopkathon.global.response.ApiResponse<TokenResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(created(authService.signUp(request)));
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 48시간짜리 access token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public org.sopt.sopkathon.global.response.ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return success(authService.login(request));
    }
}
