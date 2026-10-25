package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.response.PinnedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.PinNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.PinNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.PinNoteViewModel;
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

class PinNoteControllerTest {

    private static final AuthenticatedUser USER = () -> "owner";

    private final PinNotePort useCase = mock(PinNotePort.class);
    private final PinNoteController controller =
            new PinNoteController(useCase, new PinNotePresenter(new ErrorPresenter()), new HttpResponsePresenter());

    @Test
    @Timeout(1)
    @Tag("pinNoteController")
    void returnsUpdatedPinnedFlag() {
        when(useCase.setPinned(any())).thenReturn(Result.success(new PinnedNoteResponse("note", true)));

        var response = controller.setPinned("note", Map.of("pinned", true), USER);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(assertInstanceOf(PinNoteViewModel.class, response.getBody()).pinned());
    }

    @Test
    @Timeout(1)
    @Tag("pinNoteController")
    void rejectsMissingOrNonBooleanFlag() {
        var missing = controller.setPinned("note", Map.of(), USER);
        assertEquals(400, missing.getStatusCode().value());

        var wrongType = controller.setPinned("note", Map.of("pinned", "true"), USER);
        assertEquals(400, wrongType.getStatusCode().value());
        assertEquals("pinned must be a boolean",
                assertInstanceOf(ErrorViewModel.class, wrongType.getBody()).message());
        verify(useCase, never()).setPinned(any());
    }

    @Test
    @Timeout(1)
    @Tag("pinNoteController")
    void mapsDeletedNoteConflict() {
        when(useCase.setPinned(any())).thenReturn(Result.failure(
                ApplicationError.conflictError("deleted notes cannot be pinned")));

        assertEquals(409, controller.setPinned("note", Map.of("pinned", true), USER).getStatusCode().value());
    }
}
