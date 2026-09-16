package com.luispiquinrey.backend.notes.slices.pin;

import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
public class PinNoteController {
    private final PinNoteService service;

    public PinNoteController(PinNoteService service) {
        this.service = service;
    }

    @PatchMapping("/{id}/pin")
    public NotePinResponse setPinned(
            @PathVariable String id,
            @RequestBody NotePinRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return NotePinResponse.from(service.setPinned(id, request, authenticatedUser.accountId()));
    }
}
