package org.sopt.sopkathon.story.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import org.sopt.sopkathon.user.domain.User;

@Entity
@Table(
        name = "stories",
        indexes = {
                @Index(name = "idx_stories_spot_id", columnList = "spot_id"),
                @Index(name = "idx_stories_user_id", columnList = "user_id")
        }
)
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "story_type", nullable = false, length = 30)
    private StoryType storyType;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Story() {
    }

    private Story(Long spotId, User user, String title, String content, StoryType storyType) {
        this.spotId = spotId;
        this.user = user;
        this.title = title;
        this.content = content;
        this.storyType = storyType;
    }

    public static Story create(Long spotId, User user, String title, String content, StoryType storyType) {
        return new Story(spotId, user, title, content, storyType);
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long id() {
        return id;
    }

    public Long spotId() {
        return spotId;
    }

    public User user() {
        return user;
    }

    public String title() {
        return title;
    }

    public String content() {
        return content;
    }

    public StoryType storyType() {
        return storyType;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void assignIdForTest(Long id) {
        this.id = id;
    }
}
