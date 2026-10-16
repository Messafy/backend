package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.GetNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.presentation.dto.NoteGetResponse;
import com.luispiquinrey.backend.notes.presentation.dto.NotesByOwnerAndSharedWithRequest;
import com.luispiquinrey.backend.notes.presentation.dto.NotesByOwnerRequest;
import com.luispiquinrey.backend.notes.presentation.dto.NotesBySharedWithRequest;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class GetNoteController {

    private static final Logger log = LoggerFactory.getLogger(GetNoteController.class);

    private final GetNoteService service;

    public GetNoteController(GetNoteService service) {
        this.service = service;
    }

    @Operation(
            summary = "Get a note by its id",
            description = "Returns a single note when the authenticated account owns it or is its shared "
                    + "recipient. Otherwise responds 404 so note existence is never leaked.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note found and accessible",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteGetResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note does not exist or is not accessible (empty body)",
                    content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<NoteGetResponse> getById(
            @Parameter(description = "Note id as 24-character hexadecimal ObjectId", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to get note by id");
        Optional<Note> note = service.findById(id, authenticatedUser.accountId());
        if (note.isEmpty()) {
            log.info("Requested note was not found");
        }
        return note
                .map(found -> ResponseEntity.ok(NoteGetResponse.from(found)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "List notes by status",
            description = "Lists notes with the given status that the authenticated account owns or receives. "
                    + "Useful for admin-style views; ownership is still enforced per note.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered note list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteGetResponse.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Unknown status value. One of NEW, READ, HIDDEN, REPORTED, DELETED",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping(params = "status")
    public ResponseEntity<List<NoteGetResponse>> getByStatus(
            @Parameter(description = "Note status filter", example = "NEW")
            @RequestParam NoteStatus status,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to list notes with status {}", status);
        List<NoteGetResponse> body = service.findByStatus(status, authenticatedUser.accountId())
                .stream()
                .map(NoteGetResponse::from)
                .toList();
        log.info("Returning {} notes with status {}", body.size(), status);
        return ResponseEntity.ok(body);
    }

    @Operation(
            summary = "List trashed notes of the current account",
            description = "Returns the soft-deleted notes owned by the authenticated account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Trash list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteGetResponse.class)))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
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

    @Operation(
            summary = "List active notes of an owner",
            description = "Lists the active notes of the given owner. The owner id must be a 24-character "
                    + "hexadecimal ObjectId; results are limited to what the authenticated account may see.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Active note list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteGetResponse.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed ownerId query parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping(params = "ownerId")
    public ResponseEntity<List<NoteGetResponse>> getActiveByOwner(
            @Valid @ModelAttribute NotesByOwnerRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        String ownerId = authenticatedUser.accountId();
        log.info("Received request to list active notes for current owner");
        List<NoteGetResponse> body = service.findActiveByOwner(ownerId)
                .stream()
                .map(NoteGetResponse::from)
                .toList();
        log.info("Returning {} active notes for current owner", body.size());
        return ResponseEntity.ok(body);
    }

    @Operation(
            summary = "List active notes shared with an account",
            description = "Lists the active notes addressed to the given recipient account. The sharedWith id "
                    + "must be a 24-character hexadecimal ObjectId.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shared note list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteGetResponse.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed sharedWith query parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping(params = "sharedWith")
    public ResponseEntity<List<NoteGetResponse>> getActiveBySharedWith(
            @Valid @ModelAttribute NotesBySharedWithRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        String sharedWith = authenticatedUser.accountId();
        log.info("Received request to list active notes shared with current account");
        List<NoteGetResponse> body = service.findActiveBySharedWith(sharedWith)
                .stream()
                .map(NoteGetResponse::from)
                .toList();
        log.info("Returning {} active notes shared with current account", body.size());
        return ResponseEntity.ok(body);
    }

    @Operation(
            summary = "List active notes of an owner shared with a recipient",
            description = "Combined owner-plus-recipient filter. Both ids must be 24-character hexadecimal ObjectIds.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered note list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteGetResponse.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed ownerId or sharedWith query parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping(params = { "ownerId", "sharedWith" })
    public ResponseEntity<List<NoteGetResponse>> getActiveByOwnerAndSharedWith(
            @Valid @ModelAttribute NotesByOwnerAndSharedWithRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        String ownerId = authenticatedUser.accountId();
        String sharedWith = request.sharedWith();
        log.info("Received request to list active notes by owner and recipient");
        List<NoteGetResponse> body = service.findActiveByOwnerAndSharedWith(ownerId, sharedWith)
                .stream()
                .map(NoteGetResponse::from)
                .toList();
        log.info("Returning {} active notes by owner and recipient", body.size());
        return ResponseEntity.ok(body);
    }

}
