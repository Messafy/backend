package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.ShareNoteRequest;
import com.luispiquinrey.backend.notes.application.port.ShareNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.ShareNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.SharedNoteViewModel;
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

import java.util.Map;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class ShareNoteController {

    private static final Logger log = LoggerFactory.getLogger(ShareNoteController.class);

    private final ShareNotePort useCase;
    private final ShareNotePresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public ShareNoteController(
            ShareNotePort useCase,
            ShareNotePresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
    }

    @Operation(
            summary = "Share a private note with a recipient",
            description = "Converts an owned private note into a shared note addressed to the recipient "
                    + "identified by public code plus locker password, and notifies the recipient in real time. "
                    + "Shared notes become read-only for the owner. Neither credential is ever logged.",
            requestBody = @RequestBody(
                    description = "Recipient credentials: public code and locker password.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareNoteRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note shared",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SharedNoteViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data: unknown recipient public code or locker password mismatch",
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
                    description = "Only private notes can be shared (already shared or deleted)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @PatchMapping("/{id}/share")
    public ResponseEntity<?> share(
            @Parameter(description = "Note id", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to share note");
        ShareNoteRequest input;
        try {
            input = new ShareNoteRequest(
                    id,
                    authenticatedUser.accountId(),
                    text(body, "recipientPublicCode"),
                    text(body, "recipientLockerPassword")
            );
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }

        return httpResponsePresenter.toHttp(
                presenter.present(useCase.share(input)),
                view -> {
                    log.info("Shared note");
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
}
