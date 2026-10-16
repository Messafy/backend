package com.luispiquinrey.backend.account.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReadinessSecurityTest {
    @Test
    void onlyReadinessGetIsPublicAmongHealthEndpoints() throws Exception {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().setActiveProfiles("security-check");
            context.getEnvironment().getPropertySources().addFirst(
                    new org.springframework.core.env.MapPropertySource("test", java.util.Map.of(
                            "app.frontend-url", "http://localhost:5173")));
            context.register(SecurityConfig.class, TestConfiguration.class);
            context.refresh();
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

            mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
            mvc.perform(get("/actuator/health")).andExpect(status().isForbidden());
            mvc.perform(get("/actuator/health/liveness")).andExpect(status().isForbidden());
            mvc.perform(post("/actuator/health/readiness")).andExpect(status().isForbidden());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class TestConfiguration {
        @Bean JwtTokenVerifier jwtTokenVerifier() { return mock(JwtTokenVerifier.class); }
        @Bean UserDetailsService userDetailsService() { return mock(UserDetailsService.class); }
        @Bean RateLimiterFilter rateLimiterFilter() { return new RateLimiterFilter(); }
        @Bean HealthController healthController() { return new HealthController(); }
    }

    @RestController
    static class HealthController {
        @GetMapping("/actuator/health/readiness")
        String readiness() { return "UP"; }
    }
}
