package com.luispiquinrey.backend.account.infrastructure.config;

import com.luispiquinrey.backend.account.infrastructure.security.StompJwtAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtAuthenticationInterceptor authenticationInterceptor;
    private final String frontendUrl;

    public WebSocketConfig(StompJwtAuthenticationInterceptor authenticationInterceptor,
                           @Value("${app.frontend-url}") String frontendUrl) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendUrl);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor);
    }
}
