package org.sopt.sopkathon.reaction.repository;

import java.util.Optional;
import org.sopt.sopkathon.reaction.domain.StoryReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryReactionRepository extends JpaRepository<StoryReaction, Long> {

    boolean existsByStory_IdAndUser_Id(Long storyId, Long userId);

    Optional<StoryReaction> findByStory_IdAndUser_Id(Long storyId, Long userId);
}
