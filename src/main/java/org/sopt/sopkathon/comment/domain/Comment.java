package org.sopt.sopkathon.comment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import org.sopt.sopkathon.anonymity.domain.Anonymity;

@Entity
@Table(
        name = "comments",
        indexes = @Index(name = "idx_comments_anonymous_commenter_id", columnList = "anonymous_commenter_id")
)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anonymous_commenter_id", nullable = false)
    private Anonymity anonymousCommenter;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Comment() {
    }

    private Comment(Anonymity anonymousCommenter, String content) {
        this.anonymousCommenter = anonymousCommenter;
        this.content = content;
    }

    public static Comment create(Anonymity anonymousCommenter, String content) {
        return new Comment(anonymousCommenter, content);
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long id() {
        return id;
    }

    public Anonymity anonymousCommenter() {
        return anonymousCommenter;
    }

    public String content() {
        return content;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void assignIdForTest(Long id) {
        this.id = id;
    }
}
