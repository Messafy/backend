package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.DeleteNoteService;
import com.luispiquinrey.backend.notes.presentation.dto.NoteDeletionRequest;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
public class DeleteNoteController {

    private static final Logger log = LoggerFactory.getLogger(DeleteNoteController.class);

    private final DeleteNoteService deleteNoteService;

    public DeleteNoteController(DeleteNoteService deleteNoteService) {
        this.deleteNoteService = deleteNoteService;
    }

    @DeleteMapping("")
    public void deleteNote(
            @Valid @RequestBody NoteDeletionRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to delete note {}", request.id());
        deleteNoteService.delete(request.id(), authenticatedUser.accountId());
        log.info("Deleted note {}", request.id());
    }
}
