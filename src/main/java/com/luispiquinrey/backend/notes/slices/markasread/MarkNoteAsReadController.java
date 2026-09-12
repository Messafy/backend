package com.luispiquinrey.backend.notes.slices.markasread;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
public class MarkNoteAsReadController {

    private static final Logger log = LoggerFactory.getLogger(MarkNoteAsReadController.class);

    private final MarkNoteAsReadService service;

    public MarkNoteAsReadController(MarkNoteAsReadService service) {
        this.service = service;
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NoteMarkAsReadResponse> markAsRead(
            @Valid @ModelAttribute NoteMarkAsReadRequest request
    ) {
        String id = request.id();
        log.info("Received request to mark note {} as read", id);
        NoteMarkAsReadResponse response = NoteMarkAsReadResponse.from(service.markAsRead(id));
        log.info("Marked note {} as read at {}", id, response.readAt());
        return ResponseEntity.ok(response);
    }
}
