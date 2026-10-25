package com.luispiquinrey.backend.account.presentation.presenter;

import com.luispiquinrey.backend.account.application.dto.response.LoginAccountResponse;
import com.luispiquinrey.backend.account.presentation.viewmodel.LoginViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;

public class LoginAccountPresenter {
    private final ErrorPresenter errorPresenter;

    public LoginAccountPresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<LoginViewModel> present(Result<LoginAccountResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        return OperationResult.succeed(new LoginViewModel(result.getValue().token()));
    }
}
