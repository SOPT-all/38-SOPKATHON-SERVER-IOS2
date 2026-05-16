package org.sopt.sopkathon.support;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import org.sopt.sopkathon.global.security.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

// Controller 테스트에서 로그인 사용자가 필요한 경우 붙인다.
// 실제 JWT를 만들지 않고 SecurityContext에 AuthenticatedUser를 넣어준다.
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    long memberId() default 1L;

    String role() default "ROLE_USER";
}

class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        // 운영 코드의 principal 타입과 같은 AuthenticatedUser를 넣어 @CurrentUser 테스트까지 같이 검증한다.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthenticatedUser principal = new AuthenticatedUser(annotation.memberId(), annotation.role());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(annotation.role()))
        );
        context.setAuthentication(authentication);
        return context;
    }
}
