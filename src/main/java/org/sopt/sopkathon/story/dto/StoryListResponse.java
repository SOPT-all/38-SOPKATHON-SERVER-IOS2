package org.sopt.sopkathon.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record StoryListResponse(
        @Schema(description = "스토리 목록")
        List<StorySummaryResponse> stories
) {
}
