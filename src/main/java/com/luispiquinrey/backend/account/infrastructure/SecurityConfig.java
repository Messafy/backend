package com.luispiquinrey.backend.account.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

@Profile("!test")
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenVerifier jwtTokenVerifier,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        return new JwtAuthenticationFilter(
                jwtTokenVerifier,
                userDetailsService,
                handlerExceptionResolver
        );
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/**")
                        .permitAll()

                        .requestMatchers("/v1/admin/**")
                        .hasRole(ROLE_ADMIN)

                        .requestMatchers(HttpMethod.GET, "/v1/accounts/me")
                        .hasAnyRole(ROLE_USER, ROLE_ADMIN)

                        .requestMatchers("/v1/accounts/**")
                        .hasRole(ROLE_ADMIN)

                        .requestMatchers(HttpMethod.POST, "/v1/notes")
                        .hasAnyRole(ROLE_USER, ROLE_ADMIN)

                        .requestMatchers(HttpMethod.GET, "/v1/notes/**")
                        .hasAnyRole(ROLE_USER, ROLE_ADMIN)

                        .requestMatchers(HttpMethod.PATCH, "/v1/notes/**")
                        .hasAnyRole(ROLE_USER, ROLE_ADMIN)

                        .requestMatchers(HttpMethod.DELETE, "/v1/notes/**")
                        .hasAnyRole(ROLE_USER, ROLE_ADMIN)

                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(
                    List.of("http://localhost:5173")
            );
            corsConfiguration.setAllowedMethods(
                    List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS")
            );
            corsConfiguration.setAllowedHeaders(
                    List.of("Content-Type", "Authorization")
            );
            corsConfiguration.setAllowCredentials(true);
            return corsConfiguration;
        };
    }
}