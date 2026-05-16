package org.sopt.sopkathon.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentUserArgumentResolverTest {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("@CurrentUser AuthenticatedUser 파라미터만 지원한다")
    void supportsOnlyCurrentUserAuthenticatedUserParameter() throws Exception {
        assertThat(resolver.supportsParameter(parameter("currentUser", AuthenticatedUser.class))).isTrue();
        assertThat(resolver.supportsParameter(parameter("withoutAnnotation", AuthenticatedUser.class))).isFalse();
        assertThat(resolver.supportsParameter(parameter("wrongType", String.class))).isFalse();
    }

    @Test
    @DisplayName("SecurityContext에 인증 사용자가 있으면 AuthenticatedUser를 반환한다")
    void resolveAuthenticatedUser() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser(7L, "ROLE_USER");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority(user.role()))
        ));
        SecurityContextHolder.setContext(context);

        Object resolved = resolver.resolveArgument(
                parameter("currentUser", AuthenticatedUser.class),
                null,
                null,
                null
        );

        assertThat(resolved).isEqualTo(user);
    }

    @Test
    @DisplayName("인증 사용자가 없으면 null을 반환한다")
    void resolveNullWhenUnauthenticated() throws Exception {
        Object resolved = resolver.resolveArgument(
                parameter("currentUser", AuthenticatedUser.class),
                null,
                null,
                null
        );

        assertThat(resolved).isNull();
    }

    private MethodParameter parameter(String methodName, Class<?> parameterType) throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(methodName, parameterType);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    static class TestController {

        void currentUser(@CurrentUser AuthenticatedUser user) {
        }

        void withoutAnnotation(AuthenticatedUser user) {
        }

        void wrongType(@CurrentUser String user) {
        }
    }
}
