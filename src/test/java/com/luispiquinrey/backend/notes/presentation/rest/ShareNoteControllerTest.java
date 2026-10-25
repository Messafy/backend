package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.notes.application.port.ShareNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.ShareNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.SharedNoteViewModel;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShareNoteControllerTest {

    private static final AuthenticatedUser USER = () -> "owner";

    private final ShareNotePort useCase = mock(ShareNotePort.class);
    private final ShareNoteController controller =
            new ShareNoteController(useCase, new ShareNotePresenter(new ErrorPresenter()), new HttpResponsePresenter());

    @Test
    @Timeout(1)
    @Tag("shareNoteController")
    void returnsSharedNote() {
        when(useCase.share(any())).thenReturn(Result.success(new NoteResponse(
                "note", "Title", "Content", "NEW", "SHARED", "owner", "recipient",
                List.of(), false, Instant.parse("2026-10-25T10:00:00Z"), null)));

        var response = controller.share("note", Map.of(
                "recipientPublicCode", "V1StGXR8_Z5j",
                "recipientLockerPassword", "Locker1!"), USER);

        assertEquals(200, response.getStatusCode().value());
        var view = assertInstanceOf(SharedNoteViewModel.class, response.getBody());
        assertEquals("SHARED", view.type());
        assertEquals("recipient", view.sharedWith());
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteController")
    void validatesNoteIdBeforeCallingUseCase() {
        var response = controller.share(" ", Map.of(
                "recipientPublicCode", "V1StGXR8_Z5j",
                "recipientLockerPassword", "Locker1!"), USER);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", assertInstanceOf(ErrorViewModel.class, response.getBody()).code());
        verify(useCase, never()).share(any());
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteController")
    void mapsAlreadySharedConflict() {
        when(useCase.share(any())).thenReturn(Result.failure(
                ApplicationError.conflictError("only private notes can be shared")));

        assertEquals(409, controller.share("note", Map.of(
                "recipientPublicCode", "V1StGXR8_Z5j",
                "recipientLockerPassword", "Locker1!"), USER).getStatusCode().value());
    }
}
