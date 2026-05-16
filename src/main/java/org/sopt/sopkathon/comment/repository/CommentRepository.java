package org.sopt.sopkathon.comment.repository;

import java.util.Collection;
import java.util.List;
import org.sopt.sopkathon.comment.domain.Comment;
import org.sopt.sopkathon.story.repository.StoryCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
            select comment.anonymousCommenter.story.id as storyId, count(comment.id) as count
            from Comment comment
            where comment.anonymousCommenter.story.id in :storyIds
            group by comment.anonymousCommenter.story.id
            """)
    List<StoryCountProjection> countByStoryIdsGrouped(@Param("storyIds") Collection<Long> storyIds);
}
