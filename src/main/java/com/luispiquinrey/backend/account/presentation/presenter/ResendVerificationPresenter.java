package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.ResendVerificationResponse;
import com.luispiquinrey.backend.account.presentation.viewmodel.ResendVerificationViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;

public class ResendVerificationPresenter {

    static final String ACCEPTED_MESSAGE =
            "If an unverified account exists for this email, a new verification email is on its way";

    private final ErrorPresenter errorPresenter;

    public ResendVerificationPresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<ResendVerificationViewModel> present(Result<ResendVerificationResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        return OperationResult.succeed(new ResendVerificationViewModel(ACCEPTED_MESSAGE));
    }
}
