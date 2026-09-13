package com.luispiquinrey.backend.notes.slices.delete;

import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody NoteDeletionRequest noteDeletionRequest,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to delete note {}", noteDeletionRequest.id());
        deleteNoteService.delete(noteDeletionRequest.id(), authenticatedUser.accountId());
        log.info("Deleted note {}", noteDeletionRequest.id());
    }
}
