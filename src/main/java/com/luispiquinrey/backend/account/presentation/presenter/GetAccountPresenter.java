package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.presentation.viewmodel.AccountViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class GetAccountPresenter {
    private final ErrorPresenter errorPresenter;

    public GetAccountPresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<AccountViewModel> present(Result<AccountResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        AccountResponse response = result.getValue();
        return OperationResult.succeed(new AccountViewModel(
                response.id(), response.email(), response.publicCode(), response.role(), response.status(),
                response.verified(), formatDate(response.createdAt()), formatDate(response.lastLoginAt())));
    }

    private String formatDate(Instant date) {
        return date == null ? null : DateTimeFormatter.ISO_INSTANT.format(date);
    }
}
