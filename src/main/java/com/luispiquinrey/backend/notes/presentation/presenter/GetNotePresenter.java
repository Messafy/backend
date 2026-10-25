package com.luispiquinrey.backend.notes.presentation.presenter;

import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.notes.presentation.viewmodel.NoteViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class GetNotePresenter {

    private final ErrorPresenter errorPresenter;

    public GetNotePresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<NoteViewModel> present(Result<NoteResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        return OperationResult.succeed(toView(result.getValue()));
    }

    public OperationResult<List<NoteViewModel>> presentAll(Result<List<NoteResponse>> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        return OperationResult.succeed(result.getValue().stream().map(this::toView).toList());
    }

    private NoteViewModel toView(NoteResponse response) {
        return new NoteViewModel(
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
        );
    }
}
