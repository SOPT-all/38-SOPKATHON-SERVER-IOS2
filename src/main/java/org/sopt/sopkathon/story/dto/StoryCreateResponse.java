package org.sopt.sopkathon.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StoryCreateResponse(
        @Schema(example = "1")
        Long storyId
) {
}
