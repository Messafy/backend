package com.luispiquinrey.backend.share.presentation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class ErrorPresenterTest {
    private final ErrorPresenter presenter = new ErrorPresenter();

    @Test
    @Tag("errorPresenter")
    @Timeout(1)
    void preservesExpectedErrorMessagesAndCodes() {
        for (String code : new String[]{"VALIDATION_ERROR", "NOT_FOUND_ERROR", "UNAUTHORIZED_ERROR", "FORBIDDEN_ERROR", "CONFLICT_ERROR"}) {
            assertEquals(new ErrorViewModel("Operation failed", code), presenter.present("Operation failed", code));
        }
    }

    @Test
    @Tag("errorPresenter")
    @Timeout(1)
    void hidesInternalDiagnostics() {
        assertEquals(new ErrorViewModel("An internal error occurred", "INTERNAL_ERROR"),
                presenter.present("sensitive diagnostic", "INTERNAL_ERROR"));
    }

    @Test
    @Tag("errorPresenter")
    @Timeout(1)
    void normalizesUnknownErrorsWithoutLeakingDiagnostics() {
        assertEquals(new ErrorViewModel("An internal error occurred", "INTERNAL_ERROR"),
                presenter.present("sensitive diagnostic", "UNKNOWN_ERROR"));
    }
}
