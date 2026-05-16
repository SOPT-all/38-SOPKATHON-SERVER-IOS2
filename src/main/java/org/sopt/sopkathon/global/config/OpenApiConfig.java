package org.sopt.sopkathon.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI(
            @Value("${app.swagger.server-url}") String serverUrl,
            @Value("${app.swagger.server-description}") String serverDescription
    ) {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("SOPKATHON Backend API")
                        .description("SOPKATHON 해커톤 백엔드 API 문서입니다.")
                        .version("v1"))
                .addServersItem(new Server()
                        .url(serverUrl)
                        .description(serverDescription))
                // 전역 security를 걸지 않는다. MVP 중 공개 API까지 자물쇠가 표시되면 클라이언트가 헷갈린다.
                // 인증이 필요한 API가 생기면 해당 Controller 메서드에 @SecurityRequirement를 붙인다.
                .components(new Components().addSecuritySchemes(JWT_SCHEME_NAME, bearerScheme));
    }
}
