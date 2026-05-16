package org.sopt.sopkathon.reaction.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.sopt.sopkathon.reaction.domain.StoryReaction;
import org.sopt.sopkathon.story.repository.StoryCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryReactionRepository extends JpaRepository<StoryReaction, Long> {

    boolean existsByStory_IdAndUser_Id(Long storyId, Long userId);

    Optional<StoryReaction> findByStory_IdAndUser_Id(Long storyId, Long userId);

    @Query("""
            select reaction
            from StoryReaction reaction
            where reaction.story.id in :storyIds
              and reaction.user.id = :userId
            """)
    List<StoryReaction> findAllByStoryIdsAndUserId(
            @Param("storyIds") Collection<Long> storyIds,
            @Param("userId") Long userId
    );

    @Query("""
            select reaction.story.id as storyId, count(reaction.id) as count
            from StoryReaction reaction
            where reaction.story.id in :storyIds
            group by reaction.story.id
            """)
    List<StoryCountProjection> countByStoryIdsGrouped(@Param("storyIds") Collection<Long> storyIds);

    @Query("""
            select reaction.reactionType as reactionType, count(reaction.id) as count
            from StoryReaction reaction
            where reaction.story.id = :storyId
            group by reaction.reactionType
            """)
    List<ReactionTypeCountProjection> countByStoryIdGroupedByReactionType(@Param("storyId") Long storyId);
}
