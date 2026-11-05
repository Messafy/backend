package com.luispiquinrey.backend.share.infrastructure.web;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void returnsFieldErrorsForInvalidRequestBody() throws Exception {
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "title", "must not be blank"));
        var parameter = new MethodParameter(Object.class.getMethod("equals", Object.class), 0);
        var exception = new MethodArgumentNotValidException(parameter, binding);

        var response = handler.handleException(exception, request);

        assertEquals(400, response.getStatusCode().value());
        var problem = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("VALIDATION_ERROR", problem.getProperties().get("code"));
        assertEquals(List.of(Map.of("field", "title", "message", "must not be blank")),
                problem.getProperties().get("errors"));
        assertInstanceOf(Instant.class, problem.getProperties().get("timestamp"));
    }

    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void returnsConstraintViolationDetails() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(new InvalidRequest(""));

            var problem = handler.handleConstraintViolation(new ConstraintViolationException(violations));

            assertEquals(400, problem.getStatus());
            assertEquals("VALIDATION_ERROR", problem.getProperties().get("code"));
            assertEquals(List.of(Map.of("field", "title", "message", "Title is required")),
                    problem.getProperties().get("errors"));
        }
    }

    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void returnsBadRequestForInvalidArgument() {
        var problem = handler.handleIllegalArgument(new IllegalArgumentException("Invalid id"));

        assertEquals(400, problem.getStatus());
        assertEquals("Invalid id", problem.getDetail());
        assertEquals("Invalid id", problem.getProperties().get("message"));
        assertEquals("VALIDATION_ERROR", problem.getProperties().get("code"));
        assertInstanceOf(Instant.class, problem.getProperties().get("timestamp"));
    }

    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void technicalStateFailureIsNotExposedAsBusinessConflict() {
        var problem = handler.handleIllegalState(new IllegalStateException("internal details"));

        assertEquals(500, problem.getStatus());
        assertEquals("An internal error occurred", problem.getDetail());
        assertEquals("INTERNAL_ERROR", problem.getProperties().get("code"));
    }

    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void unexpectedFailuresKeepTheSameShapeWithoutDetails() throws Exception {
        var problem = handler.handleUnexpected(new RuntimeException("secret details"));

        assertEquals(500, problem.getStatus());
        assertEquals("An internal error occurred", problem.getProperties().get("message"));
    }

    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void securityExceptionsAreLeftToSpringSecurity() {
        var denied = new org.springframework.security.access.AccessDeniedException("denied");

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> handler.handleUnexpected(denied));
    }

    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void malformedJsonUsesTheSharedProblemShape() throws Exception {
        var exception = new HttpMessageNotReadableException("bad json",
                new MockHttpInputMessage(new byte[0]));

        var response = handler.handleException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        var problem = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("VALIDATION_ERROR", problem.getProperties().get("code"));
        assertEquals(problem.getDetail(), problem.getProperties().get("message"));
    }

    private record InvalidRequest(@NotBlank(message = "Title is required") String title) {}
}
