package com.luispiquinrey.backend.account.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Profile("test")
@Configuration
@EnableWebSecurity
public class TestSecurityConfig {

    @Value(value = "${security.user.username}")
    private String username;

    @Value(value = "${security.user.password}")
    private String password;

    @Bean
    UserDetailsService userDetailsService() {

        var user = User.withUsername(username).password(password).roles("USER").build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/**")
                        .permitAll()
                        .requestMatchers("/ws")
                        .permitAll()
                        .requestMatchers("/v1/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/accounts/me")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/v1/accounts/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/notes")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/notes/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/v1/notes/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/notes/**")
                        .hasAnyRole("USER", "ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // Mirrors the CORS policy of the production SecurityConfig so slice tests
    // prove the same contract (stateless API: CSRF disabled, see csrf() above).
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
