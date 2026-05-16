package org.sopt.sopkathon.comment.repository;

import org.sopt.sopkathon.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
