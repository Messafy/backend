package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.CreateSharedNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.CreateSharedNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.CreateSharedNoteViewModel;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateSharedNoteControllerTest {

    private static final AuthenticatedUser USER = () -> "owner";

    private final CreateSharedNotePort useCase = mock(CreateSharedNotePort.class);
    private final CreateSharedNoteController controller =
            new CreateSharedNoteController(
                    useCase, new CreateSharedNotePresenter(new ErrorPresenter()), new HttpResponsePresenter());

    @Test
    @Timeout(1)
    @Tag("createSharedNoteController")
    void returnsCreatedWithLocation() {
        when(useCase.create(any())).thenReturn(Result.success(new CreatedNoteResponse(
                "note", "SHARED", "NEW", Instant.parse("2026-10-25T10:00:00Z"))));

        var response = controller.createSharedNote(Map.of(
                "title", "Title",
                "content", "Content",
                "recipientPublicCode", "V1StGXR8_Z5j",
                "recipientLockerPassword", "Locker1!"), USER);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("/v1/notes/note", response.getHeaders().getLocation().toString());
        assertEquals("SHARED", assertInstanceOf(CreateSharedNoteViewModel.class, response.getBody()).type());
    }

    @Test
    @Timeout(1)
    @Tag("createSharedNoteController")
    void rejectsInvalidNoteDataBeforeCallingUseCase() {
        var response = controller.createSharedNote(Map.of(
                "title", " ",
                "content", "Content",
                "recipientPublicCode", "V1StGXR8_Z5j",
                "recipientLockerPassword", "Locker1!"), USER);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", assertInstanceOf(ErrorViewModel.class, response.getBody()).code());
        verify(useCase, never()).create(any());
    }

    @Test
    @Timeout(1)
    @Tag("createSharedNoteController")
    void mapsUnknownRecipientToBadRequest() {
        when(useCase.create(any())).thenReturn(Result.failure(
                ApplicationError.validationError("no active account was found for the provided public code")));

        var response = controller.createSharedNote(Map.of(
                "title", "Title",
                "content", "Content",
                "recipientPublicCode", "V1StGXR8_Z5j",
                "recipientLockerPassword", "Locker1!"), USER);

        assertEquals(400, response.getStatusCode().value());
    }
}
