package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.UpdateNoteService;
import com.luispiquinrey.backend.notes.presentation.dto.NoteUpdateRequest;
import com.luispiquinrey.backend.notes.presentation.dto.NoteUpdateResponse;
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
public class UpdateNoteController {

    private static final Logger log = LoggerFactory.getLogger(UpdateNoteController.class);

    private final UpdateNoteService service;

    public UpdateNoteController(UpdateNoteService service) {
        this.service = service;
    }

    @Operation(
            summary = "Update a private note",
            description = "Renames the title, replaces the content and optionally replaces all tags of an "
                    + "owned private note. Shared notes are read-only once sent and cannot be edited.",
            requestBody = @RequestBody(
                    description = "New title, content and optional full tag replacement.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteUpdateRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteUpdateResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid update data (blank title/content, invalid tags)",
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
                    description = "Shared notes cannot be edited after they are sent",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))})
    @PatchMapping("/{id}")
    public ResponseEntity<NoteUpdateResponse> update(
            @Parameter(description = "Note id as 24-character hexadecimal ObjectId", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody NoteUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to update note {}", id);
        NoteUpdateResponse response = NoteUpdateResponse.from(
                service.update(id, request.toCommand(), authenticatedUser.accountId())
        );
        log.info("Updated note {}", id);
        return ResponseEntity.ok(response);
    }
}
