package com.luispiquinrey.backend.notes.slices.update;

import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
public class UpdateNoteController {

    private final UpdateNoteService service;

    public UpdateNoteController(UpdateNoteService service) {
        this.service = service;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NoteUpdateResponse> update(
            @PathVariable String id,
            @Valid @RequestBody NoteUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(NoteUpdateResponse.from(
                service.update(id, request, authenticatedUser.accountId())
        ));
    }
}
