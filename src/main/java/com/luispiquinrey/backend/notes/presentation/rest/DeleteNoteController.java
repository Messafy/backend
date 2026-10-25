package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.DeleteNoteRequest;
import com.luispiquinrey.backend.notes.application.port.DeleteNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.DeleteNotePresenter;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class DeleteNoteController {

    private static final Logger log = LoggerFactory.getLogger(DeleteNoteController.class);

    private final DeleteNotePort useCase;
    private final DeleteNotePresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public DeleteNoteController(
            DeleteNotePort useCase,
            DeleteNotePresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
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
                            schema = @Schema(implementation = DeleteNoteRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Note moved to trash (empty body)",
                    content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed deletion payload",
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
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @DeleteMapping("")
    public ResponseEntity<?> deleteNote(
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to delete note");
        DeleteNoteRequest input;
        try {
            input = new DeleteNoteRequest(
                    text(body, "id"),
                    authenticatedUser.accountId()
            );
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))),
                    ignored -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
        }

        return httpResponsePresenter.toHttp(
                presenter.present(useCase.delete(input)),
                ignored -> {
                    log.info("Deleted note");
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
}
