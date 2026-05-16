package org.sopt.sopkathon.global.security;

import java.security.Principal;

// JWT 인증이 성공했을 때 SecurityContext에 들어가는 현재 사용자 정보다.
// 컨트롤러에서는 @CurrentUser로 받고, 서비스에는 보통 memberId만 넘긴다.
public record AuthenticatedUser(
        Long memberId,
        String role
) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(memberId);
    }
}
