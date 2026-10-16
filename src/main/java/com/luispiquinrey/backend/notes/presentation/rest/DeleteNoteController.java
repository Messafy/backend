package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.DeleteNoteService;
import com.luispiquinrey.backend.notes.presentation.dto.NoteDeletionRequest;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class DeleteNoteController {

    private static final Logger log = LoggerFactory.getLogger(DeleteNoteController.class);

    private final DeleteNoteService deleteNoteService;

    public DeleteNoteController(DeleteNoteService deleteNoteService) {
        this.deleteNoteService = deleteNoteService;
    }

    @Operation(
            summary = "Soft-delete a note",
            description = "Marks an owned note as DELETED instead of removing it, so it stays visible in "
                    + "the trash listing. The note id travels in the JSON body, not in the path.",
            requestBody = @RequestBody(
                    description = "Deletion payload carrying the note id.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteDeletionRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Note moved to trash (empty body)",
                    content = @Content),
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
                    description = "The authenticated account does not own the note",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No note exists with the given id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))})
    @DeleteMapping("")
    public void deleteNote(
            @Valid @org.springframework.web.bind.annotation.RequestBody NoteDeletionRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to delete note");
        deleteNoteService.delete(request.id(), authenticatedUser.accountId());
        log.info("Deleted note");
    }
}
