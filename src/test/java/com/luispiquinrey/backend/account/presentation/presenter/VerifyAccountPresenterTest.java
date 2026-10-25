package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.VerifyAccountResponse;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class VerifyAccountPresenterTest {
    @Test
    @Tag("verifyAccountPresenter")
    @Timeout(1)
    void convertsVerificationStateWithoutChangingIt() {
        var presenter = new VerifyAccountPresenter(new ErrorPresenter());

        assertTrue(presenter.present(Result.success(new VerifyAccountResponse(true))).getValue().verified());
        assertFalse(presenter.present(Result.success(new VerifyAccountResponse(false))).getValue().verified());
    }
}
