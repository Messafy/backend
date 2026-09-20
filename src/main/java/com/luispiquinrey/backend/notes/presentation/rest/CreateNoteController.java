package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.CreateNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.presentation.dto.NoteCreationRequest;
import com.luispiquinrey.backend.notes.presentation.dto.NoteCreationResponse;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/v1/notes")
public class CreateNoteController {

    private static final Logger log = LoggerFactory.getLogger(CreateNoteController.class);

    private final CreateNoteService createNoteService;

    public CreateNoteController(CreateNoteService createNoteService) {
        this.createNoteService = createNoteService;
    }

    @PostMapping
    public ResponseEntity<NoteCreationResponse> createNote(
            @Valid @RequestBody NoteCreationRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to create a {} note", request == null ? "unknown" : request.type());
        Note note = createNoteService.createNote(request.toCommand(), authenticatedUser.accountId());
        URI location = URI.create("/v1/notes/" + note.id().id());
        log.info("Created note {} and returning location {}", note.id().id(), location);

        return ResponseEntity.created(location)
                .body(NoteCreationResponse.from(note));
    }
}
