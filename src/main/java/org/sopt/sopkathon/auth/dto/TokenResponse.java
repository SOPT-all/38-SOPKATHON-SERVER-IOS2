package org.sopt.sopkathon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "API 인증에 사용하는 access token")
        String accessToken
) {

    public static TokenResponse bearer(String accessToken) {
        return new TokenResponse("Bearer", accessToken);
    }
}
