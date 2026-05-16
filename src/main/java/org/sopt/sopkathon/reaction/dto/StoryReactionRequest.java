package org.sopt.sopkathon.reaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.sopt.sopkathon.reaction.domain.ReactionType;

public record StoryReactionRequest(
        @NotNull(message = "userId는 필수입니다.")
        @Positive(message = "userId는 양수여야 합니다.")
        Long userId,

        @NotBlank(message = "reactionType은 필수입니다.")
        @Pattern(
                regexp = "LIKE|EMPATHY|SURPRISE|SAD",
                message = "reactionType은 LIKE, EMPATHY, SURPRISE, SAD 중 하나여야 합니다."
        )
        String reactionType
) {

    public ReactionType toReactionType() {
        return ReactionType.valueOf(reactionType);
    }
}
