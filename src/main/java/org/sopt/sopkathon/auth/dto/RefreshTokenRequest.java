package org.sopt.sopkathon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(description = "refresh token")
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}
