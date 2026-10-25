package com.luispiquinrey.backend.share.infrastructure.web;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
class TraceIdResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var spanContext = Span.current().getSpanContext();
        if (spanContext.isValid()) {
            response.setHeader("X-Trace-Id", spanContext.getTraceId());
        }
        filterChain.doFilter(request, response);
    }
}
