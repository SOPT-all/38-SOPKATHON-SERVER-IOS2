package org.sopt.sopkathon.global.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        boolean permitAll,
        List<String> publicEndpoints
) {

    public AppSecurityProperties {
        // yml에 public-endpoints를 비워도 NPE 없이 보안 설정이 뜨게 한다.
        if (publicEndpoints == null) {
            publicEndpoints = List.of();
        }
    }

    String[] publicEndpointMatchers() {
        return publicEndpoints.toArray(String[]::new);
    }
}
