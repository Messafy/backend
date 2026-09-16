package com.luispiquinrey.backend.notes.slices.get;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import com.luispiquinrey.backend.notes.domain.SharedNote;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/notes")
public class GetNoteController {

    private static final Logger log = LoggerFactory.getLogger(GetNoteController.class);

    private final GetNoteService service;

    public GetNoteController(GetNoteService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteGetResponse> getById(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        String safeId = sanitizeForLog(id);
        log.info("Received request to get note {}", safeId);
        Optional<Note> note = service.findById(id)
                .filter(found -> canAccess(found, authenticatedUser.accountId()));
        if (note.isEmpty()) {
            log.info("Note {} was not found", safeId);
        }
        return note
                .map(found -> ResponseEntity.ok(NoteGetResponse.from(found)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String sanitizeForLog(String value) {
        return value == null
                ? "null"
                : value.replace("\n", "_")
                .replace("\r", "_");
    }

    @GetMapping(params = "status")
    public ResponseEntity<List<NoteGetResponse>> getByStatus(
            @RequestParam NoteStatus status,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to list notes with status {}", status);
        List<NoteGetResponse> body = service.findByStatus(status)
                .stream()
                .filter(note -> canAccess(note, authenticatedUser.accountId()))
                .map(NoteGetResponse::from)
                .toList();
        log.info("Returning {} notes with status {}", body.size(), status);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/trash")
    public ResponseEntity<List<NoteGetResponse>> getTrash(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        List<NoteGetResponse> body = service.findDeletedByOwner(authenticatedUser.accountId())
                .stream()
                .map(NoteGetResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping(params = "ownerId")
    public ResponseEntity<List<NoteGetResponse>> getActiveByOwner(
            @Valid @ModelAttribute NotesByOwnerRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        String ownerId = authenticatedUser.accountId();
        log.info("Received request to list active notes for owner {}", ownerId);
        List<NoteGetResponse> body = service.findActiveByOwner(ownerId)
                .stream()
                .map(NoteGetResponse::from)
                .toList();
        log.info("Returning {} active notes for owner {}", body.size(), ownerId);
        return ResponseEntity.ok(body);
    }

    @GetMapping(params = "sharedWith")
    public ResponseEntity<List<NoteGetResponse>> getActiveBySharedWith(
            @Valid @ModelAttribute NotesBySharedWithRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        String sharedWith = authenticatedUser.accountId();
        log.info("Received request to list active notes shared with {}", sharedWith);
        List<NoteGetResponse> body = service.findActiveBySharedWith(sharedWith)
                .stream()
                .map(NoteGetResponse::from)
                .toList();
        log.info("Returning {} active notes shared with {}", body.size(), sharedWith);
        return ResponseEntity.ok(body);
    }

    @GetMapping(params = { "ownerId", "sharedWith" })
    public ResponseEntity<List<NoteGetResponse>> getActiveByOwnerAndSharedWith(
            @Valid @ModelAttribute NotesByOwnerAndSharedWithRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        String ownerId = authenticatedUser.accountId();
        String sharedWith = request.sharedWith();
        log.info("Received request to list active notes owned by {} and shared with {}", ownerId, sharedWith);
        List<NoteGetResponse> body = service.findActiveByOwnerAndSharedWith(ownerId, sharedWith)
                .stream()
                .map(NoteGetResponse::from)
                .toList();
        log.info("Returning {} active notes owned by {} and shared with {}", body.size(), ownerId, sharedWith);
        return ResponseEntity.ok(body);
    }

    private boolean canAccess(Note note, String accountId) {
        return note.isOwnedBy(accountId)
                || note instanceof SharedNote sharedNote && sharedNote.isSharedWith(accountId);
    }
}
