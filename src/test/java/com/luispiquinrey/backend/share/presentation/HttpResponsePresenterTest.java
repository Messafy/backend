package com.luispiquinrey.backend.share.presentation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class HttpResponsePresenterTest {

    private final HttpResponsePresenter presenter = new HttpResponsePresenter();

    @Test
    @Timeout(1)
    @Tag("httpResponsePresenter")
    void mapsErrorsToTheirHttpStatuses() {
        var result = OperationResult.fail(new ErrorViewModel("not found", "NOT_FOUND_ERROR"));

        ResponseEntity<?> response = presenter.toHttp(result);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        var problem = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals(404, problem.getStatus());
        assertEquals("not found", problem.getDetail());
        assertEquals("NOT_FOUND_ERROR", problem.getProperties().get("code"));
        assertEquals("not found", problem.getProperties().get("message"));
    }

    @Test
    @Timeout(1)
    @Tag("httpResponsePresenter")
    void usesEndpointSpecificSuccessResponse() {
        Object body = new Object();

        ResponseEntity<?> response = presenter.toHttp(
                OperationResult.succeed(body),
                ignored -> ResponseEntity.status(HttpStatus.CREATED).body(body));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(body, response.getBody());
    }

    @Test
    @Timeout(1)
    @Tag("httpResponsePresenter")
    void doesNotInvokeSuccessResponseForErrors() {
        AtomicBoolean invoked = new AtomicBoolean();

        ResponseEntity<?> response = presenter.toHttp(
                OperationResult.fail(new ErrorViewModel("conflict", "CONFLICT_ERROR")),
                ignored -> {
                    invoked.set(true);
                    return ResponseEntity.ok().build();
                });

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(invoked.get());
    }
}
