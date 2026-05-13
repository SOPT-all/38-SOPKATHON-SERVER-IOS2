package org.sopt.sopkathon.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ExampleRequest(
        @Schema(description = "샘플 이름", example = "SOPT")
        @NotBlank(message = "이름은 필수입니다.")
        String name
) {
}
