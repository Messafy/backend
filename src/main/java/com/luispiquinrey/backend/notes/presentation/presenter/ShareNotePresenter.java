package com.luispiquinrey.backend.notes.presentation.presenter;

import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.notes.presentation.viewmodel.SharedNoteViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ShareNotePresenter {

    private final ErrorPresenter errorPresenter;

    public ShareNotePresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<SharedNoteViewModel> present(Result<NoteResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        NoteResponse response = result.getValue();
        return OperationResult.succeed(new SharedNoteViewModel(
                response.id(),
                response.title(),
                response.content(),
                response.status(),
                response.type(),
                response.ownerId(),
                response.sharedWith(),
                response.tags(),
                response.pinned(),
                response.createdAt() == null ? null : DateTimeFormatter.ISO_INSTANT.format(response.createdAt()),
                response.readAt() == null ? null : DateTimeFormatter.ISO_INSTANT.format(response.readAt())
        ));
    }
}
