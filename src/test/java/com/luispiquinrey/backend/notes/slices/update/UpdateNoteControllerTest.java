package com.luispiquinrey.backend.notes.slices.update;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateNoteControllerTest {

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
                request,
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
