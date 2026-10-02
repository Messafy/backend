package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.CreatePrivateNoteCommand;
import com.luispiquinrey.backend.notes.application.service.CreatePrivateNoteService;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.presentation.dto.NoteCreationResponse;
import com.luispiquinrey.backend.notes.presentation.dto.PrivateNoteCreationRequest;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
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

class CreatePrivateNoteControllerTest {

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createPrivateNoteController")
    void shouldReturnCreatedWhenPrivateNoteIsValid() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        CreatePrivateNoteService service = mock(CreatePrivateNoteService.class);
        when(service.create(any(CreatePrivateNoteCommand.class), eq("507f1f77bcf86cd799439012")))
                .thenReturn(note);

        CreatePrivateNoteController controller = new CreatePrivateNoteController(service);

        PrivateNoteCreationRequest request = new PrivateNoteCreationRequest(
                "Hello",
                "Body",
                null
        );
        AuthenticatedUser authenticatedUser = () -> "507f1f77bcf86cd799439012";

        ResponseEntity<NoteCreationResponse> response = controller.createPrivateNote(request, authenticatedUser);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/v1/notes/507f1f77bcf86cd799439011", response.getHeaders().getLocation().toString());
        assertNotNull(response.getBody());
        assertEquals("507f1f77bcf86cd799439011", response.getBody().id());
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createPrivateNoteController")
    void shouldPropagateValidationExceptionFromService() {
        CreatePrivateNoteService service = mock(CreatePrivateNoteService.class);
        when(service.create(any(CreatePrivateNoteCommand.class), eq("507f1f77bcf86cd799439012")))
                .thenThrow(new NoteValidationException("note creation data cannot be null"));

        CreatePrivateNoteController controller = new CreatePrivateNoteController(service);

        PrivateNoteCreationRequest request = new PrivateNoteCreationRequest(
                "Hello",
                "Body",
                null
        );
        AuthenticatedUser authenticatedUser = () -> "507f1f77bcf86cd799439012";

        assertThrows(
                NoteValidationException.class,
                () -> controller.createPrivateNote(request, authenticatedUser)
        );
    }
}