package org.sopt.sopkathon.anonymity.domain;

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
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import org.sopt.sopkathon.story.domain.Story;
import org.sopt.sopkathon.user.domain.User;

@Entity
@Table(
        name = "anonymous_commenters",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_anonymous_commenters_story_user",
                        columnNames = {"story_id", "user_id"}
                ),
                @UniqueConstraint(
                        name = "uk_anonymous_commenters_story_number",
                        columnNames = {"story_id", "anonymous_number"}
                )
        },
        indexes = {
                @Index(name = "idx_anonymous_commenters_story_id", columnList = "story_id"),
                @Index(name = "idx_anonymous_commenters_user_id", columnList = "user_id")
        }
)
public class Anonymity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "anonymous_number", nullable = false)
    private int anonymousNumber;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Anonymity() {
    }

    private Anonymity(Story story, User user, int anonymousNumber) {
        this.story = story;
        this.user = user;
        this.anonymousNumber = anonymousNumber;
    }

    public static Anonymity create(Story story, User user, int anonymousNumber) {
        return new Anonymity(story, user, anonymousNumber);
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long id() {
        return id;
    }

    public Story story() {
        return story;
    }

    public User user() {
        return user;
    }

    public int anonymousNumber() {
        return anonymousNumber;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void assignIdForTest(Long id) {
        this.id = id;
    }
}
