package org.sopt.sopkathon.reaction.dto;

import java.util.Map;
import org.sopt.sopkathon.reaction.domain.ReactionType;

public record StoryReactionResponse(
        Long storyId,
        Map<String, Long> reactionCounts,
        ReactionType myReactionType
) {
}
