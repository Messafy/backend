package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.PinNoteService;
import com.luispiquinrey.backend.notes.presentation.dto.NotePinRequest;
import com.luispiquinrey.backend.notes.presentation.dto.NotePinResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class PinNoteController {

    private static final Logger log = LoggerFactory.getLogger(PinNoteController.class);

    private final PinNoteService service;

    public PinNoteController(PinNoteService service) {
        this.service = service;
    }

    @Operation(
            summary = "Pin or unpin a note",
            description = "Sets the pinned flag of an owned note. Deleted notes cannot be pinned.",
            requestBody = @RequestBody(
                    description = "Desired pinned state.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotePinRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pinned state updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotePinResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing pin flag in the request body",
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
                    description = "Deleted notes cannot be pinned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))})
    @PatchMapping("/{id}/pin")
    public NotePinResponse setPinned(
            @Parameter(description = "Note id as 24-character hexadecimal ObjectId", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody NotePinRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to set pinned={} on note {}", request.pinned(), id);
        NotePinResponse response = NotePinResponse.from(service.setPinned(id, request.toCommand(), authenticatedUser.accountId()));
        log.info("Set pinned={} on note {}", request.pinned(), id);
        return response;
    }
}
