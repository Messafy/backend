package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.LoginAccountResponse;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class LoginAccountPresenterTest {
    @Test
    @Tag("loginAccountPresenter")
    @Timeout(1)
    void convertsTheIssuedTokenWithoutExposingItInStringRepresentation() {
        var operation = new LoginAccountPresenter(new ErrorPresenter())
                .present(Result.success(new LoginAccountResponse("opaque-token")));
        assertTrue(operation.isSuccess());
        var view = operation.getValue();

        assertEquals("opaque-token", view.token());
        assertFalse(view.toString().contains("opaque-token"));
    }
}
