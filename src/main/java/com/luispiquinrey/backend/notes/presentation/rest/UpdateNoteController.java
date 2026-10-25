package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.UpdateNoteRequest;
import com.luispiquinrey.backend.notes.application.port.UpdateNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.UpdateNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.UpdateNoteViewModel;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class UpdateNoteController {

    private static final Logger log = LoggerFactory.getLogger(UpdateNoteController.class);

    private final UpdateNotePort useCase;
    private final UpdateNotePresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public UpdateNoteController(
            UpdateNotePort useCase,
            UpdateNotePresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
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
                            schema = @Schema(implementation = UpdateNoteRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateNoteViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid update data (blank title/content, invalid tags)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated account does not own the note",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No note exists with the given id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Shared notes cannot be edited after they are sent",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @PatchMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(description = "Note id", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to update note");
        UpdateNoteRequest input;
        try {
            input = new UpdateNoteRequest(
                    id,
                    authenticatedUser.accountId(),
                    text(body, "title"),
                    text(body, "content"),
                    tags(body)
            );
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }

        return httpResponsePresenter.toHttp(
                presenter.present(useCase.update(input)),
                view -> {
                    log.info("Updated note");
                    return ResponseEntity.ok(view);
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
