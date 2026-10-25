package com.luispiquinrey.backend.share.result;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

@Tag("result")
@Timeout(1)
class ResultTest {
    @Test
    void rejectsNullFailure() {
        assertThrows(NullPointerException.class, () -> Result.failure(null));
    }

    @Test
    void returnsSuccessfulValue() {
        Result<String> result = Result.success("account-id");

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("account-id", result.getValue());
    }

    @Test
    void allowsSuccessWithoutValue() {
        Result<Void> result = Result.success(null);

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertNull(result.getValue());
        assertThrows(IllegalStateException.class, result::getError);
    }

    @Test
    void returnsFailureError() {
        ApplicationError error = ApplicationError.notFoundError("Account not found");
        Result<String> result = Result.failure(error);

        assertTrue(result.isFailure());
        assertFalse(result.isSuccess());
        assertSame(error, result.getError());
    }

    @Test
    void rejectsValueAccessOnFailure() {
        Result<String> result = Result.failure(ApplicationError.validationError("Invalid email"));

        assertThrows(IllegalStateException.class, result::getValue);
    }

    @Test
    void rejectsErrorAccessOnSuccess() {
        Result<String> result = Result.success("account-id");

        assertThrows(IllegalStateException.class, result::getError);
    }
}
