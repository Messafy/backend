package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.CreateSharedNoteRequest;
import com.luispiquinrey.backend.notes.application.port.CreateSharedNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.CreateSharedNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.CreateSharedNoteViewModel;
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
@RequestMapping("/v1/notes/shared")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class CreateSharedNoteController {

    private static final Logger log = LoggerFactory.getLogger(CreateSharedNoteController.class);

    private final CreateSharedNotePort useCase;
    private final CreateSharedNotePresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public CreateSharedNoteController(
            CreateSharedNotePort useCase,
            CreateSharedNotePresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
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
                            schema = @Schema(implementation = CreateSharedNoteRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Shared note created",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created note (/v1/notes/{id})"),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateSharedNoteViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data: blank fields, unknown recipient public code or locker password mismatch",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @PostMapping
    public ResponseEntity<?> createSharedNote(
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to create a shared note");
        CreateSharedNoteRequest input;
        try {
            input = new CreateSharedNoteRequest(
                    authenticatedUser.accountId(),
                    text(body, "title"),
                    text(body, "content"),
                    text(body, "recipientPublicCode"),
                    text(body, "recipientLockerPassword"),
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
                    log.info("Created shared note and returning location");
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
