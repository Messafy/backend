package com.luispiquinrey.backend.notes.slices.markasread;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarkNoteAsReadControllerTest {

    private static final AuthenticatedUser AUTHENTICATED_USER = () -> "507f1f77bcf86cd799439012";

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadController")
    void shouldReturnOkWithReadTimestampWhenMarkAsReadSucceeds() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        note.markAsRead();
        MarkNoteAsReadService service = mock(MarkNoteAsReadService.class);
        when(service.markAsRead("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012")).thenReturn(note);

        ResponseEntity<NoteMarkAsReadResponse> response = new MarkNoteAsReadController(service)
                .markAsRead(new NoteMarkAsReadRequest("507f1f77bcf86cd799439011"), AUTHENTICATED_USER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("507f1f77bcf86cd799439011", response.getBody().id());
        assertNotNull(response.getBody().readAt());
        verify(service).markAsRead("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012");
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadController")
    void shouldPropagateNoteNotFoundFromService() {
        MarkNoteAsReadService service = mock(MarkNoteAsReadService.class);
        doThrow(new NoteNotFoundException("507f1f77bcf86cd799439011"))
                .when(service).markAsRead("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012");

        MarkNoteAsReadController controller = new MarkNoteAsReadController(service);

        assertThrows(
                NoteNotFoundException.class,
                () -> controller.markAsRead(new NoteMarkAsReadRequest("507f1f77bcf86cd799439011"), AUTHENTICATED_USER)
        );
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadController")
    void shouldPropagateConflictFromService() {
        MarkNoteAsReadService service = mock(MarkNoteAsReadService.class);
        doThrow(new NoteConflictException("cannot transition note from DELETED to READ"))
                .when(service).markAsRead("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012");

        MarkNoteAsReadController controller = new MarkNoteAsReadController(service);

        assertThrows(
                NoteConflictException.class,
                () -> controller.markAsRead(new NoteMarkAsReadRequest("507f1f77bcf86cd799439011"), AUTHENTICATED_USER)
        );
    }
}
