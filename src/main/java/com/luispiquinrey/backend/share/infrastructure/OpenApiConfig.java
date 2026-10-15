package com.luispiquinrey.backend.share.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI messafyOpenAPI(@Value("${app.public-base-url}") String publicBaseUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("Messafy API")
                        .version("1.0.0")
                        .description("""
                                Private notes API. Write personal notes, share them with other accounts \
                                through public codes and locker passwords, and receive shared notes in real time.""")
                        .contact(new Contact()
                                .name("Luis Piquin Rey"))
                        .license(new License()
                                .name("Private use")))
                .servers(List.of(
                        new Server()
                                .url(publicBaseUrl)
                                .description("Configured API server")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT issued by POST /v1/auth/login. \
                                        Send it as 'Authorization: Bearer <token>'.""")));
    }
}
