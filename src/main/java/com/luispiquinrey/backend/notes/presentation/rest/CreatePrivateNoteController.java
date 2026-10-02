package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.CreatePrivateNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.presentation.dto.NoteCreationResponse;
import com.luispiquinrey.backend.notes.presentation.dto.PrivateNoteCreationRequest;
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
public class CreatePrivateNoteController {

    private static final Logger log = LoggerFactory.getLogger(CreatePrivateNoteController.class);

    private final CreatePrivateNoteService createPrivateNoteService;

    public CreatePrivateNoteController(CreatePrivateNoteService createPrivateNoteService) {
        this.createPrivateNoteService = createPrivateNoteService;
    }

    @PostMapping
    public ResponseEntity<NoteCreationResponse> createPrivateNote(
            @Valid @RequestBody PrivateNoteCreationRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to create a private note");
        Note note = createPrivateNoteService.create(request.toCommand(), authenticatedUser.accountId());
        URI location = URI.create("/v1/notes/" + note.id().id());
        log.info("Created private note {} and returning location {}", note.id().id(), location);

        return ResponseEntity.created(location)
                .body(NoteCreationResponse.from(note));
    }
}