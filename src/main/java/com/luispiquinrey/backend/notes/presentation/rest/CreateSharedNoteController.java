package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.CreateSharedNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.presentation.dto.NoteCreationResponse;
import com.luispiquinrey.backend.notes.presentation.dto.SharedNoteCreationRequest;
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
@RequestMapping("/v1/notes/shared")
public class CreateSharedNoteController {

    private static final Logger log = LoggerFactory.getLogger(CreateSharedNoteController.class);

    private final CreateSharedNoteService createSharedNoteService;

    public CreateSharedNoteController(CreateSharedNoteService createSharedNoteService) {
        this.createSharedNoteService = createSharedNoteService;
    }

    @PostMapping
    public ResponseEntity<NoteCreationResponse> createSharedNote(
            @Valid @RequestBody SharedNoteCreationRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to create a shared note for recipient with public code {}", request.recipientPublicCode());
        Note note = createSharedNoteService.create(request.toCommand(), authenticatedUser.accountId());
        URI location = URI.create("/v1/notes/" + note.id().id());
        log.info("Created shared note {} and returning location {}", note.id().id(), location);

        return ResponseEntity.created(location)
                .body(NoteCreationResponse.from(note));
    }
}