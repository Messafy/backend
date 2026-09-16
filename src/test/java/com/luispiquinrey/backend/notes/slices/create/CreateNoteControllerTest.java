package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteValidationException;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateNoteControllerTest {

    @Test
    @Timeout(1)
    @Tag("createNoteController")
    void shouldReturnCreatedWhenPrivateNoteIsValid() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        CreateNoteService service = mock(CreateNoteService.class);
        when(service.createNote(any(NoteCreationRequest.class), eq("507f1f77bcf86cd799439012")))
                .thenReturn(note);

        CreateNoteController controller = new CreateNoteController(service);

        NoteCreationRequest dto = new NoteCreationRequest(
                NoteCreationRequest.NoteType.PRIVATE,
                "Hello",
                "Body",
                null
        );
        AuthenticatedUser authenticatedUser = () -> "507f1f77bcf86cd799439012";

        ResponseEntity<NoteCreationResponse> response = controller.createNote(dto, authenticatedUser);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/v1/notes/507f1f77bcf86cd799439011", response.getHeaders().getLocation().toString());
        assertNotNull(response.getBody());
        assertEquals("507f1f77bcf86cd799439011", response.getBody().id());
    }

    @Test
    @Timeout(1)
    @Tag("createNoteController")
    void shouldReturnCreatedWhenSharedNoteIsValid() {
        Note note = new NoteFactory().createSharedNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
        CreateNoteService service = mock(CreateNoteService.class);
        when(service.createNote(any(NoteCreationRequest.class), eq("507f1f77bcf86cd799439012")))
                .thenReturn(note);

        CreateNoteController controller = new CreateNoteController(service);

        NoteCreationRequest dto = new NoteCreationRequest(
                NoteCreationRequest.NoteType.SHARED,
                "Hello",
                "Body",
                "507f1f77bcf86cd799439013"
        );
        AuthenticatedUser authenticatedUser = () -> "507f1f77bcf86cd799439012";

        ResponseEntity<NoteCreationResponse> response = controller.createNote(dto, authenticatedUser);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/v1/notes/507f1f77bcf86cd799439011", response.getHeaders().getLocation().toString());
        assertNotNull(response.getBody());
        assertEquals("507f1f77bcf86cd799439011", response.getBody().id());
    }

    @Test
    @Timeout(1)
    @Tag("createNoteController")
    void shouldPropagateValidationExceptionFromService() {
        CreateNoteService service = mock(CreateNoteService.class);
        when(service.createNote(any(NoteCreationRequest.class), eq("507f1f77bcf86cd799439012")))
                .thenThrow(new NoteValidationException("note type cannot be null"));

        CreateNoteController controller = new CreateNoteController(service);

        NoteCreationRequest dto = new NoteCreationRequest(
                null,
                "Hello",
                "Body",
                null
        );
        AuthenticatedUser authenticatedUser = () -> "507f1f77bcf86cd799439012";

        assertThrows(
                NoteValidationException.class,
                () -> controller.createNote(dto, authenticatedUser)
        );
    }
}
