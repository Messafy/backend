package com.luispiquinrey.backend.share.result;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

@Tag("applicationError")
@Timeout(1)
class ApplicationErrorTest {
    @Test
    void factoriesExposeTypeAndMessage() {
        assertEquals(new ApplicationError(ErrorType.VALIDATION_ERROR, "Invalid email"),
                ApplicationError.validationError("Invalid email"));
        assertEquals(new ApplicationError(ErrorType.NOT_FOUND_ERROR, "Account not found"),
                ApplicationError.notFoundError("Account not found"));
        assertEquals(new ApplicationError(ErrorType.UNAUTHORIZED_ERROR, "Authentication required"),
                ApplicationError.unauthorizedError("Authentication required"));
        assertEquals(new ApplicationError(ErrorType.FORBIDDEN_ERROR, "Access denied"),
                ApplicationError.forbiddenError("Access denied"));
        assertEquals(new ApplicationError(ErrorType.INTERNAL_ERROR, "Operation failed"),
                ApplicationError.internalError("Operation failed"));
        assertEquals(new ApplicationError(ErrorType.CONFLICT_ERROR, "Account already exists"),
                ApplicationError.conflictError("Account already exists"));

        ApplicationError error = ApplicationError.notFoundError("Account not found");
        assertEquals(ErrorType.NOT_FOUND_ERROR, error.type());
        assertEquals("Account not found", error.message());
    }

    @Test
    void rejectsNullType() {
        assertThrows(NullPointerException.class, () -> new ApplicationError(null, "Operation failed"));
    }

    @Test
    void rejectsNullMessage() {
        assertThrows(NullPointerException.class, () -> ApplicationError.validationError(null));
    }

    @Test
    void rejectsBlankMessage() {
        assertThrows(IllegalArgumentException.class, () -> ApplicationError.validationError(""));
        assertThrows(IllegalArgumentException.class, () -> ApplicationError.validationError(" \t\n"));
    }
}
