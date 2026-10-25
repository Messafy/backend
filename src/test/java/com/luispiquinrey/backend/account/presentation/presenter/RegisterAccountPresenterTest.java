package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.presentation.viewmodel.RegisteredAccountViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RegisterAccountPresenterTest {
    private final RegisterAccountPresenter presenter = new RegisterAccountPresenter(new ErrorPresenter());

    @Test
    @Tag("registerAccountPresenter")
    @Timeout(1)
    void selectsOnlyRegistrationFieldsAndFormatsCreationDate() {
        var response = new AccountResponse("opaque-id", "user@example.com", "public-code", "USER", "ACTIVE", true,
                Instant.parse("2026-01-01T10:00:00.123Z"), Instant.parse("2026-01-02T12:30:00Z"));

        var operation = presenter.present(Result.success(response));
        assertTrue(operation.isSuccess());
        assertEquals(new RegisteredAccountViewModel("opaque-id", "user@example.com", "public-code", "USER",
                "2026-01-01T10:00:00.123Z"), operation.getValue());
    }

    @Test
    @Tag("registerAccountPresenter")
    @Timeout(1)
    void preservesAbsentCreationDate() {
        var response = new AccountResponse("opaque-id", "user@example.com", "code", "USER", "ACTIVE", false, null, null);

        assertNull(presenter.present(Result.success(response)).getValue().createdAt());
    }
}
