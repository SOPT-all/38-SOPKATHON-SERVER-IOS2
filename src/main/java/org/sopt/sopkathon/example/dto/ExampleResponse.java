package org.sopt.sopkathon.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExampleResponse(
        @Schema(description = "샘플 상태", example = "ok")
        String status,

        @Schema(description = "샘플 설명", example = "Example API is available.")
        String description
) {
}
