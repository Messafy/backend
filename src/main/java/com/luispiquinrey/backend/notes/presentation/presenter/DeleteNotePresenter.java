package com.luispiquinrey.backend.notes.presentation.presenter;

import com.luispiquinrey.backend.notes.application.dto.response.DeletedNoteResponse;
import com.luispiquinrey.backend.notes.presentation.viewmodel.DeleteNoteViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Component;

@Component
public class DeleteNotePresenter {

    private final ErrorPresenter errorPresenter;

    public DeleteNotePresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<DeleteNoteViewModel> present(Result<DeletedNoteResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        return OperationResult.succeed(new DeleteNoteViewModel(result.getValue().id()));
    }
}
