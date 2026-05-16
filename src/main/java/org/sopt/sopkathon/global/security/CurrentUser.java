package org.sopt.sopkathon.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    // Controller method parameter에 붙여서 JWT에서 꺼낸 AuthenticatedUser를 받는다.
    // permit-all 모드나 공개 API에서는 null일 수 있으므로 인증 필수 API에서만 바로 사용한다.
}
