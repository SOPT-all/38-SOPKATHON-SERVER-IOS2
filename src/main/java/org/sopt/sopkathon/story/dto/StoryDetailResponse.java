package org.sopt.sopkathon.story.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import org.sopt.sopkathon.reaction.domain.ReactionType;
import org.sopt.sopkathon.story.domain.StoryType;

public record StoryDetailResponse(
        @Schema(example = "1")
        Long storyId,

        @Schema(example = "성수동 카페거리")
        String spotName,

        @Schema(example = "1")
        Long userId,

        @Schema(example = "익명 1")
        String nickname,

        @Schema(example = "한강밤")
        String title,

        @Schema(example = "스토리 본문입니다.")
        String content,

        @Schema(example = "MEMORY")
        StoryType storyType,

        @Schema(description = "반응 타입별 개수")
        Map<ReactionType, Long> reactionCounts,

        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Schema(nullable = true, example = "LIKE")
        ReactionType myReactionType,

        @Schema(example = "3")
        long commentCount,

        @Schema(example = "2026-05-17T12:00:00Z")
        Instant createdAt
) {
}
