package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.service.CreatePrivateNoteService;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.presentation.dto.NoteCreationResponse;
import com.luispiquinrey.backend.notes.presentation.dto.PrivateNoteCreationRequest;
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
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class CreatePrivateNoteController {

    private static final Logger log = LoggerFactory.getLogger(CreatePrivateNoteController.class);

    private final CreatePrivateNoteService createPrivateNoteService;

    public CreatePrivateNoteController(CreatePrivateNoteService createPrivateNoteService) {
        this.createPrivateNoteService = createPrivateNoteService;
    }

    @Operation(
            summary = "Create a private note",
            description = "Creates a note owned by the authenticated account. Private notes are editable "
                    + "until they are shared.",
            requestBody = @RequestBody(
                    description = "Note payload: title, content and optional tags.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PrivateNoteCreationRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Private note created",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created note (/v1/notes/{id})"),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteCreationResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid note data (blank title/content, invalid tags)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @PostMapping
    public ResponseEntity<NoteCreationResponse> createPrivateNote(
            @Valid @org.springframework.web.bind.annotation.RequestBody PrivateNoteCreationRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to create a private note");
        Note note = createPrivateNoteService.create(request.toCommand(), authenticatedUser.accountId());
        URI location = URI.create("/v1/notes/" + note.id().id());
        log.info("Created private note {} and returning location {}", note.id().id(), location);

        return ResponseEntity.created(location)
                .body(NoteCreationResponse.from(note));
    }
}