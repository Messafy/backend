package com.luispiquinrey.backend.notes.presentation.presenter;

import com.luispiquinrey.backend.notes.application.dto.response.ReadNoteResponse;
import com.luispiquinrey.backend.notes.presentation.viewmodel.MarkNoteAsReadViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class MarkNoteAsReadPresenter {

    private final ErrorPresenter errorPresenter;

    public MarkNoteAsReadPresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<MarkNoteAsReadViewModel> present(Result<ReadNoteResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        ReadNoteResponse response = result.getValue();
        return OperationResult.succeed(new MarkNoteAsReadViewModel(
                response.id(),
                response.readAt() == null ? null : DateTimeFormatter.ISO_INSTANT.format(response.readAt())
        ));
    }
}
