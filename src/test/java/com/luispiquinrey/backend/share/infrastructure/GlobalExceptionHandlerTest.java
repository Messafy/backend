package com.luispiquinrey.backend.share.infrastructure;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    @Test
    void returnsFieldErrorsForInvalidRequestBody() {
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "title", "must not be blank"));
        var exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(binding);

        var response = new GlobalExceptionHandler().handleValidationError(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(400, response.getBody().get("status"));
        assertInstanceOf(LocalDateTime.class, response.getBody().get("timestamp"));
        assertEquals(List.of(Map.of("field", "title", "message", "must not be blank")),
                response.getBody().get("errors"));
    }

    @Test
    void returnsConstraintViolationDetails() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(new InvalidRequest(""));

            var response = new GlobalExceptionHandler()
                    .handleConstraintViolation(new ConstraintViolationException(violations));

            assertEquals(400, response.getStatusCode().value());
            assertEquals(400, response.getBody().get("status"));
            assertInstanceOf(LocalDateTime.class, response.getBody().get("timestamp"));
            assertEquals(List.of(Map.of("field", "title", "message", "Title is required")),
                    response.getBody().get("errors"));
        }
    }

    @Test
    void returnsBadRequestForInvalidArgument() {
        var response = new GlobalExceptionHandler().handleIllegalArgument(new IllegalArgumentException("Invalid id"));

        assertEquals(400, response.getStatus());
        assertEquals("Invalid id", response.getDetail());
        assertInstanceOf(LocalDateTime.class, response.getProperties().get("timestamp"));
    }

    private record InvalidRequest(@NotBlank(message = "Title is required") String title) {}

    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void technicalStateFailureIsNotExposedAsBusinessConflict() {
        var response = new GlobalExceptionHandler().handleIllegalState(new IllegalStateException("internal details"));
        assertEquals(500, response.getStatus());
        assertEquals("An internal error occurred", response.getDetail());
    }
}
