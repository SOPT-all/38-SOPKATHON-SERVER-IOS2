package org.sopt.sopkathon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "student@sopt.org")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
