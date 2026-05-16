package org.sopt.sopkathon.story.repository;

import java.util.List;
import java.util.Optional;
import org.sopt.sopkathon.spot.domain.Spot;
import org.sopt.sopkathon.story.domain.Story;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryRepository extends JpaRepository<Story, Long> {

    List<Story> findAllBySpotOrderByCreatedAtDescIdDesc(Spot spot);

    @Query("""
            select story
            from Story story
            left join StoryReaction reaction on reaction.story = story
            where story.spot = :spot
            group by story
            order by count(reaction.id) desc, story.createdAt desc, story.id desc
            """)
    List<Story> findAllBySpotOrderByPopularity(@Param("spot") Spot spot);

    @EntityGraph(attributePaths = {"spot", "user"})
    @Query("select story from Story story where story.id = :storyId")
    Optional<Story> findWithSpotAndUserById(@Param("storyId") Long storyId);
}
