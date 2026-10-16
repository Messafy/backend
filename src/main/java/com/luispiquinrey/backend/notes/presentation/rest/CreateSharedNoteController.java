package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.CreateSharedNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.presentation.dto.NoteCreationResponse;
import com.luispiquinrey.backend.notes.presentation.dto.SharedNoteCreationRequest;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/v1/notes/shared")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class CreateSharedNoteController {

    private static final Logger log = LoggerFactory.getLogger(CreateSharedNoteController.class);

    private final CreateSharedNoteService createSharedNoteService;

    public CreateSharedNoteController(CreateSharedNoteService createSharedNoteService) {
        this.createSharedNoteService = createSharedNoteService;
    }

    @Operation(
            summary = "Create a shared note for a recipient",
            description = "Creates a note owned by the authenticated account and addressed to the recipient "
                    + "identified by public code plus locker password. The recipient is notified in real time. "
                    + "Neither the public code nor the locker password is ever logged.",
            requestBody = @RequestBody(
                    description = "Shared note payload: title, content, optional tags, recipient public code "
                            + "and recipient locker password.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SharedNoteCreationRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Shared note created",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created note (/v1/notes/{id})"),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteCreationResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data: blank fields, unknown recipient public code or locker password mismatch",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @PostMapping
    public ResponseEntity<NoteCreationResponse> createSharedNote(
            @Valid @org.springframework.web.bind.annotation.RequestBody SharedNoteCreationRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to create a shared note");
        Note note = createSharedNoteService.create(request.toCommand(), authenticatedUser.accountId());
        URI location = URI.create("/v1/notes/" + note.id().id());
        log.info("Created shared note and returning location");

        return ResponseEntity.created(location)
                .body(NoteCreationResponse.from(note));
    }
}
