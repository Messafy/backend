package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.ErrorType;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountPresenterOperationResultTest {
    @Test
    @Tag("accountPresenterOperationResult")
    @Timeout(1)
    void everyPresenterTranslatesApplicationFailuresIntoPublicOperationErrors() {
        var errors = new ErrorPresenter();
        var get = new GetAccountPresenter(errors);
        var register = new RegisterAccountPresenter(errors);
        var login = new LoginAccountPresenter(errors);
        var verify = new VerifyAccountPresenter(errors);

        for (ErrorType type : ErrorType.values()) {
            var applicationError = new ApplicationError(type, "Operation failed");
            var operations = List.of(
                    get.present(Result.failure(applicationError)),
                    register.present(Result.failure(applicationError)),
                    login.present(Result.failure(applicationError)),
                    verify.present(Result.failure(applicationError)));
            var expected = new ErrorViewModel(type == ErrorType.INTERNAL_ERROR
                    ? "An internal error occurred" : "Operation failed", type.name());

            for (var operation : operations) {
                assertTrue(operation.isFailure());
                assertFalse(operation.isSuccess());
                assertEquals(expected, operation.getError());
                assertThrows(IllegalStateException.class, operation::getValue);
            }
        }
    }
}
