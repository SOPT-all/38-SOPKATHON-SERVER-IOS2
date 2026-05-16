package org.sopt.sopkathon.story.repository;

import org.sopt.sopkathon.story.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {
}
