package org.sopt.sopkathon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "API 인증에 사용하는 access token")
        String accessToken,

        @Schema(description = "access token 재발급에 사용하는 refresh token")
        String refreshToken
) {

    public static TokenResponse bearer(String accessToken, String refreshToken) {
        return new TokenResponse("Bearer", accessToken, refreshToken);
    }
}
