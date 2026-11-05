package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.ListNotesRequest;
import com.luispiquinrey.backend.notes.application.dto.response.NotePageResponse;
import com.luispiquinrey.backend.notes.domain.repository.PageQuery;
import com.luispiquinrey.backend.notes.presentation.viewmodel.NotePageViewModel;
import org.springframework.http.ProblemDetail;
import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.notes.application.port.GetNotePort;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.presentation.presenter.GetNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.NoteViewModel;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetNoteControllerTest {

    private static final AuthenticatedUser USER = () -> "owner";

    private final GetNotePort useCase = mock(GetNotePort.class);
    private final GetNoteController controller =
            new GetNoteController(useCase, new GetNotePresenter(new ErrorPresenter()), new HttpResponsePresenter());

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void returnsSingleNote() {
        when(useCase.findById(any())).thenReturn(Result.success(noteResponse()));

        var response = controller.getById("note", USER);

        assertEquals(200, response.getStatusCode().value());
        var view = assertInstanceOf(NoteViewModel.class, response.getBody());
        assertEquals("note", view.id());
        assertEquals("PRIVATE", view.type());
        assertEquals("2026-10-25T10:00:00Z", view.createdAt());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void mapsMissingOrInaccessibleNoteToNotFound() {
        when(useCase.findById(any())).thenReturn(Result.failure(ApplicationError.notFoundError("missing")));

        var response = controller.getById("note", USER);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("NOT_FOUND_ERROR", assertInstanceOf(ProblemDetail.class, response.getBody()).getProperties().get("code"));
    }

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void validatesBlankPathId() {
        var response = controller.getById(" ", USER);

        assertEquals(400, response.getStatusCode().value());
        verify(useCase, never()).findById(any());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void listsByStatusAndMapsToViewModels() {
        when(useCase.findByStatus(any())).thenReturn(Result.success(new NotePageResponse(List.of(noteResponse()), 0, 1, 1)));

        var response = controller.getByStatus(NoteStatus.NEW, USER);

        assertEquals(200, response.getStatusCode().value());
        var views = assertInstanceOf(List.class, response.getBody());
        assertEquals(1, views.size());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void rejectsMalformedOwnerIdBeforeCallingUseCase() {
        var response = controller.getActiveByOwner(" ", USER);

        assertEquals(400, response.getStatusCode().value());
        verify(useCase, never()).findActiveByOwner(any());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void listsMyNotesAsAPageWithDefaults() {
        var request = org.mockito.ArgumentCaptor.forClass(ListNotesRequest.class);
        when(useCase.findActiveByOwner(request.capture()))
                .thenReturn(Result.success(new NotePageResponse(List.of(noteResponse()), 0, 20, 21)));

        var response = controller.getMine(null, null, null, USER);

        assertEquals(200, response.getStatusCode().value());
        var page = assertInstanceOf(NotePageViewModel.class, response.getBody());
        assertEquals(1, page.items().size());
        assertEquals(21, page.totalItems());
        assertEquals(2, page.totalPages());
        assertEquals(new PageQuery(0, 20), request.getValue().page());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void filtersMyNotesByStatusWhenRequested() {
        when(useCase.findByStatus(any())).thenReturn(Result.success(new NotePageResponse(List.of(), 1, 5, 5)));

        var response = controller.getMine(NoteStatus.READ, 1, 5, USER);

        assertEquals(200, response.getStatusCode().value());
        verify(useCase, never()).findActiveByOwner(any());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void rejectsOutOfRangePageSizeBeforeCallingUseCase() {
        var response = controller.getInbox(0, PageQuery.MAX_SIZE + 1, USER);

        assertEquals(400, response.getStatusCode().value());
        verify(useCase, never()).findActiveBySharedWith(any());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteController")
    void rejectsNegativePageBeforeCallingUseCase() {
        var response = controller.getDeleted(-1, 10, USER);

        assertEquals(400, response.getStatusCode().value());
        verify(useCase, never()).findDeletedByOwner(any());
    }

    private NoteResponse noteResponse() {
        return new NoteResponse(
                "note", "Title", "Content", "NEW", "PRIVATE", "owner", null,
                List.of("work"), false, Instant.parse("2026-10-25T10:00:00Z"), null);
    }
}
