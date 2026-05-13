package org.sopt.sopkathon.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
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
                        .description("1박2일 해커톤에서 웹/Android/iOS 팀과 빠르게 협업하기 위한 백엔드 API 문서입니다.")
                        .version("v1"))
                .addServersItem(new Server()
                        .url(serverUrl)
                        .description(serverDescription))
                .components(new Components().addSecuritySchemes(JWT_SCHEME_NAME, bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME_NAME));
    }
}
