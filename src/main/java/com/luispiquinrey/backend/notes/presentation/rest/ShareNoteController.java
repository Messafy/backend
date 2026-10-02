package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.ShareNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.presentation.dto.NoteGetResponse;
import com.luispiquinrey.backend.notes.presentation.dto.ShareNoteRequest;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
public class ShareNoteController {

    private static final Logger log = LoggerFactory.getLogger(ShareNoteController.class);

    private final ShareNoteService service;

    public ShareNoteController(ShareNoteService service) {
        this.service = service;
    }

    @PatchMapping("/{id}/share")
    public ResponseEntity<NoteGetResponse> share(
            @PathVariable String id,
            @Valid @RequestBody ShareNoteRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to share note {} with recipient {}", id, request.recipientPublicCode());
        Note note = service.share(id, request.toCommand(), authenticatedUser.accountId());
        log.info("Shared note {} with recipient {}", note.id().id(), request.recipientPublicCode());
        return ResponseEntity.ok(NoteGetResponse.from(note));
    }
}
