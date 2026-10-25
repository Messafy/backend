package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.presentation.viewmodel.RegisteredAccountViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;

import java.time.format.DateTimeFormatter;

public class RegisterAccountPresenter {
    private final ErrorPresenter errorPresenter;

    public RegisterAccountPresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<RegisteredAccountViewModel> present(Result<AccountResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        AccountResponse response = result.getValue();
        return OperationResult.succeed(new RegisteredAccountViewModel(
                response.id(), response.email(), response.publicCode(), response.role(),
                response.createdAt() == null ? null : DateTimeFormatter.ISO_INSTANT.format(response.createdAt())));
    }
}
