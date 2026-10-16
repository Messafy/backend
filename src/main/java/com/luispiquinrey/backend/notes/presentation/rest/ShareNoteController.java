package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.ShareNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.presentation.dto.NoteGetResponse;
import com.luispiquinrey.backend.notes.presentation.dto.ShareNoteRequest;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class ShareNoteController {

    private static final Logger log = LoggerFactory.getLogger(ShareNoteController.class);

    private final ShareNoteService service;

    public ShareNoteController(ShareNoteService service) {
        this.service = service;
    }

    @Operation(
            summary = "Share a private note with a recipient",
            description = "Converts an owned private note into a shared note addressed to the recipient "
                    + "identified by public code plus locker password, and notifies the recipient in real time. "
                    + "Shared notes become read-only for the owner. Neither credential is ever logged.",
            requestBody = @RequestBody(
                    description = "Recipient credentials: public code and locker password.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareNoteRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note shared",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteGetResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data: blank fields, unknown recipient public code or locker password mismatch",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated account does not own the note",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No note exists with the given id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Only private notes can be shared (already shared or deleted)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))})
    @PatchMapping("/{id}/share")
    public ResponseEntity<NoteGetResponse> share(
            @Parameter(description = "Note id as 24-character hexadecimal ObjectId", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ShareNoteRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to share note");
        Note note = service.share(id, request.toCommand(), authenticatedUser.accountId());
        log.info("Shared note");
        return ResponseEntity.ok(NoteGetResponse.from(note));
    }
}
