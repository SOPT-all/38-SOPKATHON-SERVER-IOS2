package org.sopt.sopkathon.global.security;

import java.security.Principal;

public record AuthenticatedUser(
        Long memberId,
        String role
) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(memberId);
    }
}
