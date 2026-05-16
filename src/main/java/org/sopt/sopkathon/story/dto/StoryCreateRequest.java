package org.sopt.sopkathon.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.sopt.sopkathon.story.domain.StoryType;

public record StoryCreateRequest(
        @Schema(example = "1")
        @NotNull(message = "작성자 ID는 필수입니다.")
        @Positive(message = "작성자 ID는 양수여야 합니다.")
        Long userId,

        @Schema(description = "스토리 제목. 5글자 이하, 띄어쓰기 불가", example = "한강밤")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 5, message = "제목은 5글자 이하여야 합니다.")
        @Pattern(regexp = "\\S+", message = "제목에는 띄어쓰기를 사용할 수 없습니다.")
        String title,

        @Schema(example = "스토리 본문입니다.")
        @NotBlank(message = "본문은 필수입니다.")
        String content,

        @Schema(example = "MEMORY")
        @NotNull(message = "스토리 유형은 필수입니다.")
        StoryType storyType
) {
}
