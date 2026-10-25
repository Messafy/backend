package com.luispiquinrey.backend.notes.presentation.presenter;

import com.luispiquinrey.backend.notes.application.dto.response.UpdatedNoteResponse;
import com.luispiquinrey.backend.notes.presentation.viewmodel.UpdateNoteViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Component;

@Component
public class UpdateNotePresenter {

    private final ErrorPresenter errorPresenter;

    public UpdateNotePresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<UpdateNoteViewModel> present(Result<UpdatedNoteResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        UpdatedNoteResponse response = result.getValue();
        return OperationResult.succeed(new UpdateNoteViewModel(
                response.id(),
                response.title(),
                response.content()
        ));
    }
}
