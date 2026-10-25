package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.CreatePrivateNoteRequest;
import com.luispiquinrey.backend.notes.application.port.CreatePrivateNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.CreatePrivateNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.CreatePrivateNoteViewModel;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class CreatePrivateNoteController {

    private static final Logger log = LoggerFactory.getLogger(CreatePrivateNoteController.class);

    private final CreatePrivateNotePort useCase;
    private final CreatePrivateNotePresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public CreatePrivateNoteController(
            CreatePrivateNotePort useCase,
            CreatePrivateNotePresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
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
                            schema = @Schema(implementation = CreatePrivateNoteRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Private note created",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created note (/v1/notes/{id})"),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreatePrivateNoteViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid note data (blank title/content, invalid tags)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @PostMapping
    public ResponseEntity<?> createPrivateNote(
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to create a private note");
        CreatePrivateNoteRequest input;
        try {
            input = new CreatePrivateNoteRequest(
                    authenticatedUser.accountId(),
                    text(body, "title"),
                    text(body, "content"),
                    tags(body)
            );
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))),
                    view -> ResponseEntity.status(HttpStatus.CREATED)
                            .location(URI.create("/v1/notes/" + view.id()))
                            .body(view));
        }

        return httpResponsePresenter.toHttp(
                presenter.present(useCase.create(input)),
                view -> {
                    log.info("Created private note and returning location");
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .location(URI.create("/v1/notes/" + view.id()))
                            .body(view);
                });
    }

    private String text(Map<String, Object> body, String field) {
        Object value = body == null ? null : body.get(field);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException(field + " must be a string");
        }
        return text;
    }

    private List<String> tags(Map<String, Object> body) {
        Object value = body == null ? null : body.get("tags");
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> values)) {
            throw new IllegalArgumentException("tags must be a list of strings");
        }
        return values.stream().map(item -> {
            if (!(item instanceof String tag)) {
                throw new IllegalArgumentException("tags must be a list of strings");
            }
            return tag;
        }).toList();
    }
}
