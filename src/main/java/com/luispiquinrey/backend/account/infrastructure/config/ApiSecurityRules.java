package com.luispiquinrey.backend.account.infrastructure.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

final class ApiSecurityRules {

    static final String ROLE_ADMIN = "ADMIN";
    static final String ROLE_USER = "USER";

    private ApiSecurityRules() {
    }

    static void authorize(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth
                .requestMatchers(HttpMethod.GET, "/actuator/health/readiness").permitAll()
                .requestMatchers("/v1/auth/**").permitAll()
                .requestMatchers("/ws").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/v1/admin/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.GET, "/v1/accounts/me").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                .requestMatchers("/v1/accounts/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/v1/notes", "/v1/notes/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                .requestMatchers(HttpMethod.GET, "/v1/notes", "/v1/notes/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                .requestMatchers(HttpMethod.PATCH, "/v1/notes/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/v1/notes/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                .anyRequest().authenticated();
    }

    static CorsConfigurationSource cors(String frontendUrl) {
        return request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(List.of(frontendUrl));
            corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
            corsConfiguration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
            corsConfiguration.setAllowCredentials(true);
            return corsConfiguration;
        };
    }
}
