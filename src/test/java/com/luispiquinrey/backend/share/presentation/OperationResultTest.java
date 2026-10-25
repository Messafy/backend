package com.luispiquinrey.backend.share.presentation;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class OperationResultTest {
    @Test
    @Tag("operationResult")
    @Timeout(1)
    void exposesOnlyTheSuccessfulViewModel() {
        var view = java.util.Map.of("id", "opaque-id");
        var result = OperationResult.succeed(view);

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertSame(view, result.getValue());
        assertThrows(IllegalStateException.class, result::getError);
    }

    @Test
    @Tag("operationResult")
    @Timeout(1)
    void exposesOnlyTheFailureViewModel() {
        var error = new ErrorViewModel("Invalid input", "VALIDATION_ERROR");
        OperationResult<String> result = OperationResult.fail(error);

        assertTrue(result.isFailure());
        assertFalse(result.isSuccess());
        assertSame(error, result.getError());
        assertThrows(IllegalStateException.class, result::getValue);
    }

    @Test
    @Tag("operationResult")
    @Timeout(1)
    void rejectsNullOutcomes() {
        assertThrows(NullPointerException.class, () -> OperationResult.succeed(null));
        assertThrows(NullPointerException.class, () -> OperationResult.fail(null));
    }

    @Test
    @Tag("operationResult")
    @Timeout(1)
    void keepsTokensOutOfStringRepresentations() {
        assertFalse(OperationResult.succeed("sensitive-token").toString().contains("sensitive-token"));
    }

    @Test
    @Tag("operationResult")
    @Timeout(1)
    void rejectsIncompletePresentationErrors() {
        assertThrows(NullPointerException.class, () -> new ErrorViewModel(null, "VALIDATION_ERROR"));
        assertThrows(NullPointerException.class, () -> new ErrorViewModel("Invalid input", null));
        assertThrows(IllegalArgumentException.class, () -> new ErrorViewModel(" ", "VALIDATION_ERROR"));
        assertThrows(IllegalArgumentException.class, () -> new ErrorViewModel("Invalid input", " "));
    }
}
