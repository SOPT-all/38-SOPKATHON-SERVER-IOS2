package org.sopt.sopkathon.reaction.application;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.sopt.sopkathon.global.error.BusinessException;
import org.sopt.sopkathon.reaction.domain.ReactionType;
import org.sopt.sopkathon.reaction.domain.StoryReaction;
import org.sopt.sopkathon.reaction.dto.StoryReactionRequest;
import org.sopt.sopkathon.reaction.dto.StoryReactionResponse;
import org.sopt.sopkathon.reaction.repository.ReactionTypeCountProjection;
import org.sopt.sopkathon.reaction.repository.StoryReactionRepository;
import org.sopt.sopkathon.story.domain.Story;
import org.sopt.sopkathon.story.error.StoryErrorCode;
import org.sopt.sopkathon.story.repository.StoryRepository;
import org.sopt.sopkathon.user.domain.User;
import org.sopt.sopkathon.user.error.UserErrorCode;
import org.sopt.sopkathon.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StoryReactionService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final StoryReactionRepository storyReactionRepository;

    public StoryReactionService(
            StoryRepository storyRepository,
            UserRepository userRepository,
            StoryReactionRepository storyReactionRepository
    ) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.storyReactionRepository = storyReactionRepository;
    }

    @Transactional
    public StoryReactionResponse react(Long storyId, StoryReactionRequest request) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new BusinessException(StoryErrorCode.STORY_NOT_FOUND));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        ReactionType myReactionType = updateReaction(story, user, request.toReactionType());

        return new StoryReactionResponse(
                story.id(),
                getReactionCounts(story.id()),
                myReactionType
        );
    }

    public Map<String, Long> getReactionCounts(Long storyId) {
        Map<ReactionType, Long> counted = new EnumMap<>(ReactionType.class);
        for (ReactionTypeCountProjection row : storyReactionRepository.countByStoryIdGroupedByReactionType(storyId)) {
            counted.put(row.getReactionType(), row.getCount());
        }

        Map<String, Long> reactionCounts = new LinkedHashMap<>();
        for (ReactionType reactionType : ReactionType.values()) {
            reactionCounts.put(reactionType.name(), counted.getOrDefault(reactionType, 0L));
        }
        return reactionCounts;
    }

    public ReactionType getMyReactionTypeOrNull(Long storyId, Long userId) {
        return storyReactionRepository.findByStory_IdAndUser_Id(storyId, userId)
                .map(StoryReaction::reactionType)
                .orElse(null);
    }

    private ReactionType updateReaction(Story story, User user, ReactionType requestedReactionType) {
        Optional<StoryReaction> existingReaction = storyReactionRepository.findByStory_IdAndUser_Id(
                story.id(),
                user.id()
        );

        if (existingReaction.isEmpty()) {
            storyReactionRepository.save(StoryReaction.create(story, user, requestedReactionType));
            return requestedReactionType;
        }

        StoryReaction reaction = existingReaction.get();
        if (reaction.reactionType() == requestedReactionType) {
            storyReactionRepository.delete(reaction);
            return null;
        }

        reaction.changeReactionType(requestedReactionType);
        return requestedReactionType;
    }
}
