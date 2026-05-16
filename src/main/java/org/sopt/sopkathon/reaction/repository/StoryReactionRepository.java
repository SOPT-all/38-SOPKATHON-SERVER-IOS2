package org.sopt.sopkathon.reaction.repository;

import java.util.List;
import java.util.Optional;
import org.sopt.sopkathon.reaction.domain.ReactionType;
import org.sopt.sopkathon.reaction.domain.StoryReaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface StoryReactionRepository extends JpaRepository<StoryReaction, Long> {

    boolean existsByStory_IdAndUser_Id(Long storyId, Long userId);

    Optional<StoryReaction> findByStory_IdAndUser_Id(Long storyId, Long userId);

    @Query("""
            select sr.reactionType as reactionType, count(sr) as count
            from StoryReaction sr
            where sr.story.id = :storyId
            group by sr.reactionType
            """)
    List<ReactionTypeCount> countByStoryIdGroupByReactionType(@Param("storyId") Long storyId);

    interface ReactionTypeCount {

        ReactionType getReactionType();

        long getCount();
    }
}
