package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.notes.domain.Note;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<NoteCreationResponse> createNote(@Valid @RequestBody NoteCreationRequest noteCreationRequest) {
        log.info("Received request to create a {} note", noteCreationRequest == null ? "unknown" : noteCreationRequest.type());
        Note note = createNoteService.createNote(noteCreationRequest);
        URI location = URI.create("/v1/notes/" + note.id().id());
        log.info("Created note {} and returning location {}", note.id().id(), location);

        return ResponseEntity.created(location)
                .body(NoteCreationResponse.from(note));
    }
}
