package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.MarkNoteAsReadRequest;
import com.luispiquinrey.backend.notes.application.port.MarkNoteAsReadPort;
import com.luispiquinrey.backend.notes.presentation.presenter.MarkNoteAsReadPresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.MarkNoteAsReadViewModel;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class MarkNoteAsReadController {

    private static final Logger log = LoggerFactory.getLogger(MarkNoteAsReadController.class);

    private final MarkNoteAsReadPort useCase;
    private final MarkNoteAsReadPresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public MarkNoteAsReadController(
            MarkNoteAsReadPort useCase,
            MarkNoteAsReadPresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
    }

    @Operation(
            summary = "Mark a note as read",
            description = "Records the read timestamp of a note. Allowed for the owner and for the shared "
                    + "recipient. The note id travels in the path (/{id}/read).")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note marked as read, receipt returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MarkNoteAsReadViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed note id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "403",
                    description = "Neither the owner nor the shared recipient",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No note exists with the given id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @Parameter(description = "Note id", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to mark note as read");
        MarkNoteAsReadRequest input;
        try {
            input = new MarkNoteAsReadRequest(id, authenticatedUser.accountId());
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }

        return httpResponsePresenter.toHttp(
                presenter.present(useCase.markAsRead(input)),
                view -> {
                    log.info("Marked note as read");
                    return ResponseEntity.ok(view);
                });
    }
}
