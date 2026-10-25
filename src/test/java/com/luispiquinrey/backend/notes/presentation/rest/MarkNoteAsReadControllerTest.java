package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.response.ReadNoteResponse;
import com.luispiquinrey.backend.notes.application.port.MarkNoteAsReadPort;
import com.luispiquinrey.backend.notes.presentation.presenter.MarkNoteAsReadPresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.MarkNoteAsReadViewModel;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarkNoteAsReadControllerTest {

    private static final AuthenticatedUser USER = () -> "owner";

    private final MarkNoteAsReadPort useCase = mock(MarkNoteAsReadPort.class);
    private final MarkNoteAsReadController controller =
            new MarkNoteAsReadController(
                    useCase, new MarkNoteAsReadPresenter(new ErrorPresenter()), new HttpResponsePresenter());

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadController")
    void returnsReadReceipt() {
        when(useCase.markAsRead(any())).thenReturn(Result.success(
                new ReadNoteResponse("note", Instant.parse("2026-10-25T10:00:00Z"))));

        var response = controller.markAsRead("note", USER);

        assertEquals(200, response.getStatusCode().value());
        var view = assertInstanceOf(MarkNoteAsReadViewModel.class, response.getBody());
        assertEquals("note", view.id());
        assertEquals("2026-10-25T10:00:00Z", view.readAt());
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadController")
    void validatesBlankNoteId() {
        var response = controller.markAsRead(" ", USER);

        assertEquals(400, response.getStatusCode().value());
        verify(useCase, never()).markAsRead(any());
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadController")
    void mapsForbiddenAndNotFoundErrors() {
        when(useCase.markAsRead(any())).thenReturn(Result.failure(
                ApplicationError.forbiddenError("only the owner or the shared recipient can mark a note as read")));
        assertEquals(403, controller.markAsRead("note", USER).getStatusCode().value());

        when(useCase.markAsRead(any())).thenReturn(Result.failure(
                ApplicationError.notFoundError("note not found")));
        var response = controller.markAsRead("note", USER);
        assertEquals(404, response.getStatusCode().value());
        assertEquals("NOT_FOUND_ERROR", assertInstanceOf(ErrorViewModel.class, response.getBody()).code());
    }
}
