package org.sopt.sopkathon.reaction.repository;

import org.sopt.sopkathon.reaction.domain.ReactionType;

public interface ReactionTypeCountProjection {

    ReactionType getReactionType();

    long getCount();
}
