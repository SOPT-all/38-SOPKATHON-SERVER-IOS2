package org.sopt.sopkathon.reaction.domain;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import org.sopt.sopkathon.story.domain.Story;
import org.sopt.sopkathon.user.domain.User;

@Entity
@Table(
        name = "story_reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_story_reactions_story_user",
                columnNames = {"story_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_story_reactions_story_id", columnList = "story_id"),
                @Index(name = "idx_story_reactions_user_id", columnList = "user_id")
        }
)
public class StoryReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 30)
    private ReactionType reactionType;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected StoryReaction() {
    }

    private StoryReaction(Story story, User user, ReactionType reactionType) {
        this.story = story;
        this.user = user;
        this.reactionType = reactionType;
    }

    public static StoryReaction create(Story story, User user, ReactionType reactionType) {
        return new StoryReaction(story, user, reactionType);
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public void changeReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
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

    public ReactionType reactionType() {
        return reactionType;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void assignIdForTest(Long id) {
        this.id = id;
    }
}
