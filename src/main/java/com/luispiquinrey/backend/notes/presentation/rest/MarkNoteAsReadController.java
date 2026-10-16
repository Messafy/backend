package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.MarkNoteAsReadService;
import com.luispiquinrey.backend.notes.presentation.dto.NoteMarkAsReadRequest;
import com.luispiquinrey.backend.notes.presentation.dto.NoteMarkAsReadResponse;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class MarkNoteAsReadController {

    private static final Logger log = LoggerFactory.getLogger(MarkNoteAsReadController.class);

    private final MarkNoteAsReadService service;

    public MarkNoteAsReadController(MarkNoteAsReadService service) {
        this.service = service;
    }

    @Operation(
            summary = "Mark a note as read",
            description = "Records the read timestamp of a note. Allowed for the owner and for the shared "
                    + "recipient. The note id travels in the path (/{id}/read) and is bound from it.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note marked as read, receipt returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteMarkAsReadResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed note id (must be a 24-character hexadecimal ObjectId)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "403",
                    description = "Neither the owner nor the shared recipient",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No note exists with the given id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))})
    @PatchMapping("/{id}/read")
    public ResponseEntity<NoteMarkAsReadResponse> markAsRead(
            @Valid @ModelAttribute NoteMarkAsReadRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        String id = request.id();
        log.info("Received request to mark note as read");
        NoteMarkAsReadResponse response = NoteMarkAsReadResponse.from(
                service.markAsRead(id, authenticatedUser.accountId()));
        log.info("Marked note as read");
        return ResponseEntity.ok(response);
    }
}
