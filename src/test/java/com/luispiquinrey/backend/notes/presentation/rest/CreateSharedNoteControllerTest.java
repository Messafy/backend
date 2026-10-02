package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.CreateSharedNoteCommand;
import com.luispiquinrey.backend.notes.application.service.CreateSharedNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.presentation.dto.NoteCreationResponse;
import com.luispiquinrey.backend.notes.presentation.dto.SharedNoteCreationRequest;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateSharedNoteControllerTest {

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createSharedNoteController")
    void shouldReturnCreatedWhenSharedNoteIsValid() {
        Note note = new NoteFactory().createSharedNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
        CreateSharedNoteService service = mock(CreateSharedNoteService.class);
        when(service.create(any(CreateSharedNoteCommand.class), eq("507f1f77bcf86cd799439012")))
                .thenReturn(note);

        CreateSharedNoteController controller = new CreateSharedNoteController(service);

        SharedNoteCreationRequest request = new SharedNoteCreationRequest(
                "Hello",
                "Body",
                "V1StGXR8_Z5j",
                "Locker1!",
                null
        );
        AuthenticatedUser authenticatedUser = () -> "507f1f77bcf86cd799439012";

        ResponseEntity<NoteCreationResponse> response = controller.createSharedNote(request, authenticatedUser);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/v1/notes/507f1f77bcf86cd799439011", response.getHeaders().getLocation().toString());
        assertNotNull(response.getBody());
        assertEquals("507f1f77bcf86cd799439011", response.getBody().id());
    }
}