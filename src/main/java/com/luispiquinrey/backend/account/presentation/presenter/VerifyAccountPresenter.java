package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.VerifyAccountResponse;
import com.luispiquinrey.backend.account.presentation.viewmodel.VerificationViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;

public class VerifyAccountPresenter {
    private final ErrorPresenter errorPresenter;

    public VerifyAccountPresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<VerificationViewModel> present(Result<VerifyAccountResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        return OperationResult.succeed(new VerificationViewModel(result.getValue().verified()));
    }
}
