package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.share.presentation.Problems;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class SecurityProblemResponses {

    static final String UNAUTHORIZED_MESSAGE = "authentication is required";
    static final String FORBIDDEN_MESSAGE = "access is denied";

    private SecurityProblemResponses() {
    }

    public static AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> write(response, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
    }

    public static AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> write(response, HttpStatus.FORBIDDEN, FORBIDDEN_MESSAGE);
    }

    static void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write((
                "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,"
                        + "\"detail\":\"%s\",\"code\":\"%s\",\"message\":\"%s\"}"
        ).formatted(status.getReasonPhrase(), status.value(), message, Problems.codeFor(status), message));
    }
}
