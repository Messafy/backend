package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.PinNoteRequest;
import com.luispiquinrey.backend.notes.application.port.PinNotePort;
import com.luispiquinrey.backend.notes.presentation.presenter.PinNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.PinNoteViewModel;
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
public class PinNoteController {

    private static final Logger log = LoggerFactory.getLogger(PinNoteController.class);

    private final PinNotePort useCase;
    private final PinNotePresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public PinNoteController(
            PinNotePort useCase,
            PinNotePresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
    }

    @Operation(
            summary = "Pin or unpin a note",
            description = "Sets the pinned flag of an owned note. Deleted notes cannot be pinned.",
            requestBody = @RequestBody(
                    description = "Desired pinned state.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PinNoteRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pinned state updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PinNoteViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid pin flag in the request body",
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
                    description = "Deleted notes cannot be pinned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @PatchMapping("/{id}/pin")
    public ResponseEntity<?> setPinned(
            @Parameter(description = "Note id", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to update note pinned state");
        PinNoteRequest input;
        try {
            input = new PinNoteRequest(id, authenticatedUser.accountId(), flag(body, "pinned"));
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }

        return httpResponsePresenter.toHttp(
                presenter.present(useCase.setPinned(input)),
                view -> {
                    log.info("Updated note pinned state");
                    return ResponseEntity.ok(view);
                });
    }

    private Boolean flag(Map<String, Object> body, String field) {
        Object value = body == null ? null : body.get(field);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Boolean flag)) {
            throw new IllegalArgumentException(field + " must be a boolean");
        }
        return flag;
    }
}
