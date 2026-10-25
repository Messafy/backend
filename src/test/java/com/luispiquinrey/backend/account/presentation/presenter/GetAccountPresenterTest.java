package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.presentation.viewmodel.AccountViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class GetAccountPresenterTest {
    private final GetAccountPresenter presenter = new GetAccountPresenter(new ErrorPresenter());

    @Test
    @Tag("getAccountPresenter")
    @Timeout(1)
    void convertsProfileFieldsAndFormatsDates() {
        var response = new AccountResponse("opaque-id", "user@example.com", "public-code", "USER", "ACTIVE", true,
                Instant.parse("2026-01-01T10:00:00.123Z"), Instant.parse("2026-01-02T12:30:00Z"));

        var operation = presenter.present(Result.success(response));
        assertTrue(operation.isSuccess());
        assertEquals(new AccountViewModel("opaque-id", "user@example.com", "public-code", "USER", "ACTIVE", true,
                "2026-01-01T10:00:00.123Z", "2026-01-02T12:30:00Z"), operation.getValue());
    }

    @Test
    @Tag("getAccountPresenter")
    @Timeout(1)
    void preservesAbsentDatesAndAccountState() {
        var response = new AccountResponse("opaque-id", "user@example.com", "code", "USER", "DELETED", false, null, null);

        var view = presenter.present(Result.success(response)).getValue();

        assertNull(view.createdAt());
        assertNull(view.lastLoginAt());
        assertEquals("DELETED", view.status());
        assertFalse(view.verified());
    }
}
