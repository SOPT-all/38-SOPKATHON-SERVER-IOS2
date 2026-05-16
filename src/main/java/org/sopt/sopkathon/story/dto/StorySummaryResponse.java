package org.sopt.sopkathon.story.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.reaction.domain.ReactionType;
import org.sopt.sopkathon.story.domain.StoryType;

public record StorySummaryResponse(
        @Schema(example = "1")
        Long storyId,

        @Schema(example = "성수동 카페거리")
        String spotName,

        @Schema(example = "한강밤")
        String title,

        @Schema(example = "한강에서 밤새고 런...")
        String preview,

        @Schema(example = "MEMORY")
        StoryType storyType,

        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Schema(nullable = true, example = "LIKE")
        ReactionType myReactionType,

        @Schema(example = "3")
        long commentCount
) {
}
