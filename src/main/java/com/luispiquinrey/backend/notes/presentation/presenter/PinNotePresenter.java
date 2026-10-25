package com.luispiquinrey.backend.notes.presentation.presenter;

import com.luispiquinrey.backend.notes.application.dto.response.PinnedNoteResponse;
import com.luispiquinrey.backend.notes.presentation.viewmodel.PinNoteViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Component;

@Component
public class PinNotePresenter {

    private final ErrorPresenter errorPresenter;

    public PinNotePresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<PinNoteViewModel> present(Result<PinnedNoteResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        PinnedNoteResponse response = result.getValue();
        return OperationResult.succeed(new PinNoteViewModel(response.id(), response.pinned()));
    }
}
