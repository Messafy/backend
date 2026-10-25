package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.CreatePrivateNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.CreatePrivateNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.CreatePrivateNoteViewModel;
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

class CreatePrivateNoteControllerTest {

    private static final AuthenticatedUser USER = () -> "owner";

    private final CreatePrivateNotePort useCase = mock(CreatePrivateNotePort.class);
    private final CreatePrivateNoteController controller =
            new CreatePrivateNoteController(
                    useCase, new CreatePrivateNotePresenter(new ErrorPresenter()), new HttpResponsePresenter());

    @Test
    @Timeout(1)
    @Tag("createPrivateNoteController")
    void returnsCreatedWithLocationAndViewModel() {
        when(useCase.create(any())).thenReturn(Result.success(new CreatedNoteResponse(
                "note", "PRIVATE", "NEW", Instant.parse("2026-10-25T10:00:00Z"))));

        var response = controller.createPrivateNote(
                Map.of("title", "Title", "content", "Content", "tags", List.of("work")), USER);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("/v1/notes/note", response.getHeaders().getLocation().toString());
        var view = assertInstanceOf(CreatePrivateNoteViewModel.class, response.getBody());
        assertEquals("note", view.id());
        assertEquals("PRIVATE", view.type());
        assertEquals("2026-10-25T10:00:00Z", view.createdAt());
    }

    @Test
    @Timeout(1)
    @Tag("createPrivateNoteController")
    void validatesRequestBeforeCallingUseCase() {
        var response = controller.createPrivateNote(Map.of("title", " ", "content", "Content"), USER);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", assertInstanceOf(ErrorViewModel.class, response.getBody()).code());
        verify(useCase, never()).create(any());
    }

    @Test
    @Timeout(1)
    @Tag("createPrivateNoteController")
    void mapsUnexpectedBodyTypesToValidationErrors() {
        var response = controller.createPrivateNote(Map.of("title", 12, "content", "Content"), USER);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("title must be a string",
                assertInstanceOf(ErrorViewModel.class, response.getBody()).message());
    }

    @Test
    @Timeout(1)
    @Tag("createPrivateNoteController")
    void mapsUseCaseErrorsToHttp() {
        when(useCase.create(any())).thenReturn(Result.failure(ApplicationError.conflictError("conflict")));

        var response = controller.createPrivateNote(Map.of("title", "Title", "content", "Content"), USER);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("CONFLICT_ERROR", assertInstanceOf(ErrorViewModel.class, response.getBody()).code());
    }
}
