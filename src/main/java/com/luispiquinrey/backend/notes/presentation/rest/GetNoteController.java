package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.GetNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.request.ListNotesRequest;
import com.luispiquinrey.backend.notes.application.port.GetNotePort;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.presentation.presenter.GetNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.NoteViewModel;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class GetNoteController {

    private static final Logger log = LoggerFactory.getLogger(GetNoteController.class);

    private final GetNotePort useCase;
    private final GetNotePresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public GetNoteController(
            GetNotePort useCase,
            GetNotePresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
    }

    @Operation(
            summary = "Get a note by its id",
            description = "Returns a single note when the authenticated account owns it or is its shared "
                    + "recipient. Otherwise responds 404 so note existence is never leaked.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note found and accessible",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoteViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note does not exist or is not accessible",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Note id", example = "507f1f77bcf86cd799439011")
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to get note by id");
        GetNoteRequest input;
        try {
            input = new GetNoteRequest(id, authenticatedUser.accountId());
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }

        var operation = presenter.present(useCase.findById(input));
        if (operation.isFailure()) {
            log.info("Requested note was not found");
        }
        return httpResponsePresenter.toHttp(
                operation,
                view -> {
                    log.info("Returning note");
                    return ResponseEntity.ok(view);
                });
    }

    @Operation(
            summary = "List notes by status",
            description = "Lists notes with the given status that the authenticated account owns or receives.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered note list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteViewModel.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Unknown status value. One of NEW, READ, HIDDEN, REPORTED, DELETED",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping(params = "status")
    public ResponseEntity<?> getByStatus(
            @Parameter(description = "Note status filter", example = "NEW")
            @RequestParam NoteStatus status,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to list notes with status {}", status);
        ListNotesRequest input = new ListNotesRequest(authenticatedUser.accountId(), status, null);
        return httpResponsePresenter.toHttp(
                presenter.presentAll(useCase.findByStatus(input)),
                notes -> {
                    log.info("Returning {} notes", notes.size());
                    return ResponseEntity.ok(notes);
                });
    }

    @Operation(
            summary = "List trashed notes of the current account",
            description = "Returns the soft-deleted notes owned by the authenticated account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Trash list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteViewModel.class)))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping("/trash")
    public ResponseEntity<?> getTrash(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        ListNotesRequest input = new ListNotesRequest(authenticatedUser.accountId(), null, null);
        return httpResponsePresenter.toHttp(
                presenter.presentAll(useCase.findDeletedByOwner(input)),
                notes -> {
                    log.info("Returning {} notes", notes.size());
                    return ResponseEntity.ok(notes);
                });
    }

    @Operation(
            summary = "List active notes of the current account",
            description = "Lists the active notes owned by the authenticated account. The ownerId query "
                    + "parameter identifies the endpoint; ownership always comes from the JWT.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Active note list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteViewModel.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed ownerId query parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping(params = "ownerId")
    public ResponseEntity<?> getActiveByOwner(
            @RequestParam String ownerId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to list active notes for current owner");
        ListNotesRequest input;
        try {
            new UserId(ownerId);
            input = new ListNotesRequest(authenticatedUser.accountId(), null, null);
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.presentAll(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        return httpResponsePresenter.toHttp(
                presenter.presentAll(useCase.findActiveByOwner(input)),
                notes -> {
                    log.info("Returning {} notes", notes.size());
                    return ResponseEntity.ok(notes);
                });
    }

    @Operation(
            summary = "List active notes shared with the current account",
            description = "Lists the active notes addressed to the authenticated account. The sharedWith "
                    + "query parameter identifies the endpoint; the recipient always comes from the JWT.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shared note list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteViewModel.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed sharedWith query parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping(params = "sharedWith")
    public ResponseEntity<?> getActiveBySharedWith(
            @RequestParam String sharedWith,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to list active notes shared with current account");
        ListNotesRequest input;
        try {
            new UserId(sharedWith);
            input = new ListNotesRequest(authenticatedUser.accountId(), null, null);
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.presentAll(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        return httpResponsePresenter.toHttp(
                presenter.presentAll(useCase.findActiveBySharedWith(input)),
                notes -> {
                    log.info("Returning {} notes", notes.size());
                    return ResponseEntity.ok(notes);
                });
    }

    @Operation(
            summary = "List active notes of the current account shared with a recipient",
            description = "Combined owner-plus-recipient filter. The owner comes from the JWT and the "
                    + "recipient from the sharedWith query parameter.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered note list (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoteViewModel.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed ownerId or sharedWith query parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json"))})
    @GetMapping(params = { "ownerId", "sharedWith" })
    public ResponseEntity<?> getActiveByOwnerAndSharedWith(
            @RequestParam String ownerId,
            @RequestParam String sharedWith,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Received request to list active notes by owner and recipient");
        ListNotesRequest input;
        try {
            new UserId(ownerId);
            input = new ListNotesRequest(authenticatedUser.accountId(), null, sharedWith);
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.presentAll(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        return httpResponsePresenter.toHttp(
                presenter.presentAll(useCase.findActiveByOwnerAndSharedWith(input)),
                notes -> {
                    log.info("Returning {} notes", notes.size());
                    return ResponseEntity.ok(notes);
                });
    }
}
