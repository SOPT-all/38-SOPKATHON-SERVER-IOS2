package org.sopt.sopkathon.story.application;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.sopt.sopkathon.comment.repository.CommentRepository;
import org.sopt.sopkathon.global.error.BusinessException;
import org.sopt.sopkathon.reaction.domain.ReactionType;
import org.sopt.sopkathon.reaction.domain.StoryReaction;
import org.sopt.sopkathon.reaction.repository.ReactionTypeCountProjection;
import org.sopt.sopkathon.reaction.repository.StoryReactionRepository;
import org.sopt.sopkathon.spot.domain.Spot;
import org.sopt.sopkathon.spot.error.SpotErrorCode;
import org.sopt.sopkathon.spot.repository.SpotRepository;
import org.sopt.sopkathon.story.domain.Story;
import org.sopt.sopkathon.story.domain.StorySort;
import org.sopt.sopkathon.story.dto.StoryCreateRequest;
import org.sopt.sopkathon.story.dto.StoryCreateResponse;
import org.sopt.sopkathon.story.dto.StoryDetailResponse;
import org.sopt.sopkathon.story.dto.StoryListResponse;
import org.sopt.sopkathon.story.dto.StorySummaryResponse;
import org.sopt.sopkathon.story.error.StoryErrorCode;
import org.sopt.sopkathon.story.repository.StoryCountProjection;
import org.sopt.sopkathon.story.repository.StoryRepository;
import org.sopt.sopkathon.user.domain.User;
import org.sopt.sopkathon.user.error.UserErrorCode;
import org.sopt.sopkathon.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StoryService {

    private static final Long DEFAULT_SPOT_ID = 1L;
    private static final int PREVIEW_LENGTH = 10;
    private static final String PREVIEW_SUFFIX = "...";

    private final StoryRepository storyRepository;
    private final StoryReactionRepository storyReactionRepository;
    private final CommentRepository commentRepository;
    private final SpotRepository spotRepository;
    private final UserRepository userRepository;

    public StoryService(
            StoryRepository storyRepository,
            StoryReactionRepository storyReactionRepository,
            CommentRepository commentRepository,
            SpotRepository spotRepository,
            UserRepository userRepository
    ) {
        this.storyRepository = storyRepository;
        this.storyReactionRepository = storyReactionRepository;
        this.commentRepository = commentRepository;
        this.spotRepository = spotRepository;
        this.userRepository = userRepository;
    }

    public StoryListResponse getStories(Long userId, String sortValue) {
        User user = getUser(userId);
        Spot spot = getDefaultSpot();
        StorySort sort = StorySort.from(sortValue);

        List<Story> stories = switch (sort) {
            case LATEST -> storyRepository.findAllBySpotOrderByCreatedAtDescIdDesc(spot);
            case POPULAR -> storyRepository.findAllBySpotOrderByPopularity(spot);
        };

        List<Long> storyIds = stories.stream()
                .map(Story::id)
                .toList();
        Map<Long, Long> commentCounts = getCommentCounts(storyIds);
        Map<Long, ReactionType> myReactionTypes = getMyReactionTypes(storyIds, user.id());

        List<StorySummaryResponse> responses = stories.stream()
                .map(story -> new StorySummaryResponse(
                        story.id(),
                        spot.name(),
                        story.title(),
                        preview(story.content()),
                        story.storyType(),
                        myReactionTypes.get(story.id()),
                        commentCounts.getOrDefault(story.id(), 0L)
                ))
                .toList();

        return new StoryListResponse(responses);
    }

    public StoryDetailResponse getStory(Long storyId, Long userId) {
        User user = getUser(userId);
        Story story = storyRepository.findWithSpotAndUserById(storyId)
                .orElseThrow(() -> new BusinessException(StoryErrorCode.STORY_NOT_FOUND));

        Map<ReactionType, Long> reactionCounts = getReactionCounts(story.id());
        Map<Long, Long> commentCounts = getCommentCounts(List.of(story.id()));
        ReactionType myReactionType = storyReactionRepository.findByStory_IdAndUser_Id(story.id(), user.id())
                .map(StoryReaction::reactionType)
                .orElse(null);

        return new StoryDetailResponse(
                story.id(),
                story.spot().name(),
                story.user().id(),
                nickname(story.user()),
                story.title(),
                story.content(),
                story.storyType(),
                reactionCounts,
                myReactionType,
                commentCounts.getOrDefault(story.id(), 0L),
                story.createdAt()
        );
    }

    @Transactional
    public StoryCreateResponse createStory(StoryCreateRequest request) {
        User user = getUser(request.userId());
        Spot spot = getDefaultSpot();

        Story story = Story.create(
                spot,
                user,
                request.title(),
                request.content(),
                request.storyType()
        );
        Story savedStory = storyRepository.save(story);
        return new StoryCreateResponse(savedStory.id());
    }

    private Spot getDefaultSpot() {
        return spotRepository.findById(DEFAULT_SPOT_ID)
                .orElseThrow(() -> new BusinessException(SpotErrorCode.SPOT_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private Map<Long, Long> getCommentCounts(Collection<Long> storyIds) {
        if (storyIds.isEmpty()) {
            return Map.of();
        }

        return commentRepository.countByStoryIdsGrouped(storyIds)
                .stream()
                .collect(Collectors.toMap(StoryCountProjection::getStoryId, StoryCountProjection::getCount));
    }

    private Map<Long, ReactionType> getMyReactionTypes(Collection<Long> storyIds, Long userId) {
        if (storyIds.isEmpty()) {
            return Map.of();
        }

        return storyReactionRepository.findAllByStoryIdsAndUserId(storyIds, userId)
                .stream()
                .collect(Collectors.toMap(reaction -> reaction.story().id(), StoryReaction::reactionType));
    }

    private Map<ReactionType, Long> getReactionCounts(Long storyId) {
        Map<ReactionType, Long> counts = Arrays.stream(ReactionType.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        reactionType -> 0L,
                        Long::sum,
                        () -> new EnumMap<>(ReactionType.class)
                ));

        for (ReactionTypeCountProjection count : storyReactionRepository.countByStoryIdGroupedByReactionType(storyId)) {
            counts.put(count.getReactionType(), count.getCount());
        }
        return counts;
    }

    private String preview(String content) {
        int endIndex = Math.min(PREVIEW_LENGTH, content.length());
        return content.substring(0, endIndex) + PREVIEW_SUFFIX;
    }

    private String nickname(User user) {
        if (user.nickname() == null || user.nickname().isBlank()) {
            return "익명 " + user.id();
        }
        return user.nickname();
    }
}
