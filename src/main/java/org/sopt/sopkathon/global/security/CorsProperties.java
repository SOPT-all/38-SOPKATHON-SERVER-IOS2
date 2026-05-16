package org.sopt.sopkathon.global.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        // 브라우저 기반 웹 클라이언트 주소다. 배포 URL이 생기면 쉼표로 추가한다.
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials
) {
}
