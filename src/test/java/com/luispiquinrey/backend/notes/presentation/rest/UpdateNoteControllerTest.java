package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.UpdateNoteCommand;
import com.luispiquinrey.backend.notes.application.service.UpdateNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.presentation.dto.NoteUpdateRequest;
import com.luispiquinrey.backend.notes.presentation.dto.NoteUpdateResponse;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateNoteControllerTest {

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldNotLogUntrustedNoteId(CapturedOutput output) {
        String id = "note\r\nFORGED_NOTE";
        AuthenticatedUser authenticatedUser = () -> "owner";
        NoteUpdateRequest request = new NoteUpdateRequest("Title", "Content");
        UpdateNoteCommand command = new UpdateNoteCommand(request.title(), request.content(), request.tags());
        Note note = new NoteFactory().createPrivateNote("note", "Title", "Content", "owner");
        UpdateNoteService service = mock(UpdateNoteService.class);
        when(service.update(id, command, "owner")).thenReturn(note);

        var response = new UpdateNoteController(service).update(id, request, authenticatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).update(id, command, "owner");
        assertFalse(output.getAll().contains("FORGED_NOTE"));
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteController")
    void shouldReturnUpdatedNote() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Updated title",
                "Updated content",
                "507f1f77bcf86cd799439012"
        );
        UpdateNoteService service = mock(UpdateNoteService.class);
        NoteUpdateRequest request = new NoteUpdateRequest("Updated title", "Updated content");
        AuthenticatedUser authenticatedUser = () -> "507f1f77bcf86cd799439012";
        when(service.update(
                "507f1f77bcf86cd799439011",
                new UpdateNoteCommand(request.title(), request.content(), request.tags()),
                authenticatedUser.accountId()
        )).thenReturn(note);

        ResponseEntity<NoteUpdateResponse> response = new UpdateNoteController(service)
                .update("507f1f77bcf86cd799439011", request, authenticatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("507f1f77bcf86cd799439011", response.getBody().id());
        assertEquals("Updated title", response.getBody().title());
        assertEquals("Updated content", response.getBody().content());
    }
}
