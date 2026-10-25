package com.luispiquinrey.backend.notes.presentation.presenter;

import com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse;
import com.luispiquinrey.backend.notes.presentation.viewmodel.CreateSharedNoteViewModel;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class CreateSharedNotePresenter {

    private final ErrorPresenter errorPresenter;

    public CreateSharedNotePresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }

    public OperationResult<CreateSharedNoteViewModel> present(Result<CreatedNoteResponse> result) {
        if (result.isFailure()) {
            var error = result.getError();
            return OperationResult.fail(errorPresenter.present(error.message(), error.type().name()));
        }
        CreatedNoteResponse response = result.getValue();
        return OperationResult.succeed(new CreateSharedNoteViewModel(
                response.id(),
                response.type(),
                response.status(),
                response.createdAt() == null ? null : DateTimeFormatter.ISO_INSTANT.format(response.createdAt())
        ));
    }
}
