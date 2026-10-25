package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.response.UpdatedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.UpdateNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.UpdateNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.UpdateNoteViewModel;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateNoteControllerTest {

    private static final AuthenticatedUser USER = () -> "owner";

    private final UpdateNotePort useCase = mock(UpdateNotePort.class);
    private final UpdateNoteController controller =
            new UpdateNoteController(useCase, new UpdateNotePresenter(new ErrorPresenter()), new HttpResponsePresenter());

    @Test
    @Timeout(1)
    @Tag("updateNoteController")
    void returnsUpdatedNote() {
        when(useCase.update(any())).thenReturn(Result.success(
                new UpdatedNoteResponse("note", "New title", "New content")));

        var response = controller.update("note", Map.of(
                "title", "New title", "content", "New content", "tags", List.of("work")), USER);

        assertEquals(200, response.getStatusCode().value());
        var view = assertInstanceOf(UpdateNoteViewModel.class, response.getBody());
        assertEquals("New title", view.title());
        assertEquals("New content", view.content());
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteController")
    void validatesBodyBeforeCallingUseCase() {
        var response = controller.update("note", Map.of("title", " ", "content", "Content"), USER);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", assertInstanceOf(ErrorViewModel.class, response.getBody()).code());
        verify(useCase, never()).update(any());
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteController")
    void mapsSharedNoteConflictToConflict() {
        when(useCase.update(any())).thenReturn(Result.failure(
                ApplicationError.conflictError("shared notes cannot be edited")));

        var response = controller.update("note", Map.of("title", "Title", "content", "Content"), USER);

        assertEquals(409, response.getStatusCode().value());
    }
}
