package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.response.DeletedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.DeleteNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.DeleteNotePresenter;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteNoteControllerTest {

    private static final AuthenticatedUser USER = () -> "owner";

    private final DeleteNotePort useCase = mock(DeleteNotePort.class);
    private final DeleteNoteController controller =
            new DeleteNoteController(useCase, new DeleteNotePresenter(new ErrorPresenter()), new HttpResponsePresenter());

    @Test
    @Timeout(1)
    @Tag("deleteNoteController")
    void returnsNoContentOnSuccess() {
        when(useCase.delete(any())).thenReturn(Result.success(new DeletedNoteResponse("note")));

        var response = controller.deleteNote(Map.of("id", "note"), USER);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteController")
    void validatesMissingIdBeforeCallingUseCase() {
        var response = controller.deleteNote(Map.of("title", "missing"), USER);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", assertInstanceOf(ErrorViewModel.class, response.getBody()).code());
        verify(useCase, never()).delete(any());
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteController")
    void mapsForbiddenAndNotFoundErrors() {
        when(useCase.delete(any())).thenReturn(Result.failure(ApplicationError.forbiddenError("forbidden")));
        assertEquals(403, controller.deleteNote(Map.of("id", "note"), USER).getStatusCode().value());

        when(useCase.delete(any())).thenReturn(Result.failure(ApplicationError.notFoundError("missing")));
        assertEquals(404, controller.deleteNote(Map.of("id", "note"), USER).getStatusCode().value());
    }
}
