package com.luispiquinrey.backend.notes.presentation.rest;

import com.luispiquinrey.backend.notes.application.dto.request.GetNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.request.ListNotesRequest;
import com.luispiquinrey.backend.notes.application.dto.response.NotePageResponse;
import com.luispiquinrey.backend.notes.application.port.GetNotePort;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.PageQuery;
import com.luispiquinrey.backend.notes.presentation.presenter.GetNotePresenter;
import com.luispiquinrey.backend.notes.presentation.viewmodel.NotePageViewModel;
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
import java.util.function.Function;

@RestController
@RequestMapping("/v1/notes")
@Tag(name = "Notes", description = "Private and shared notes: creation, listing, update, pin, share, read receipts and deletion.")
@SecurityRequirement(name = "bearerAuth")
public class GetNoteController {

    private static final Logger log = LoggerFactory.getLogger(GetNoteController.class);
    private static final String PAGE_DESCRIPTION = "Zero-based page number (default 0)";
    private static final String SIZE_DESCRIPTION = "Page size, 1 to 100 (default 20)";

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
            deprecated = true,
            summary = "List notes by status",
            description = "Deprecated: use GET /v1/notes/mine?status=. Lists every match as a plain array.")
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
            deprecated = true,
            summary = "List trashed notes of the current account",
            description = "Deprecated: use GET /v1/notes/deleted. Lists every match as a plain array.")
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
            deprecated = true,
            summary = "List active notes of the current account",
            description = "Deprecated: use GET /v1/notes/mine. The ownerId query parameter is ignored; "
                    + "ownership always comes from the JWT. Lists every match as a plain array.")
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
            deprecated = true,
            summary = "List active notes shared with the current account",
            description = "Deprecated: use GET /v1/notes/inbox. The sharedWith query parameter is ignored; "
                    + "the recipient always comes from the JWT. Lists every match as a plain array.")
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
            deprecated = true,
            summary = "List active notes of the current account shared with a recipient",
            description = "Deprecated: use GET /v1/notes/sent?to=. The owner comes from the JWT and the "
                    + "recipient from the sharedWith query parameter. Lists every match as a plain array.")
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

    @Operation(
            summary = "List my notes",
            description = "Active notes the authenticated account owns or receives, pinned first and then "
                    + "newest first. Pass status to filter by one status instead (owned or received).")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "One page of notes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotePageViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status, page or size",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @GetMapping("/mine")
    public ResponseEntity<?> getMine(
            @Parameter(description = "Optional status filter", example = "NEW")
            @RequestParam(required = false) NoteStatus status,
            @Parameter(description = PAGE_DESCRIPTION) @RequestParam(required = false) Integer page,
            @Parameter(description = SIZE_DESCRIPTION) @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return listPage(authenticatedUser, status, null, page, size,
                status == null ? useCase::findActiveByOwner : useCase::findByStatus);
    }

    @Operation(
            summary = "List notes received by me",
            description = "Active notes shared with the authenticated account, pinned first and then newest first.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "One page of notes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotePageViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid page or size",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(
            @Parameter(description = PAGE_DESCRIPTION) @RequestParam(required = false) Integer page,
            @Parameter(description = SIZE_DESCRIPTION) @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return listPage(authenticatedUser, null, null, page, size, useCase::findActiveBySharedWith);
    }

    @Operation(
            summary = "List notes I sent to an account",
            description = "Active notes the authenticated account shared with the given recipient.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "One page of notes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotePageViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed recipient id, page or size",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @GetMapping("/sent")
    public ResponseEntity<?> getSent(
            @Parameter(description = "Recipient account id", example = "507f1f77bcf86cd799439013")
            @RequestParam String to,
            @Parameter(description = PAGE_DESCRIPTION) @RequestParam(required = false) Integer page,
            @Parameter(description = SIZE_DESCRIPTION) @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return listPage(authenticatedUser, null, to, page, size, useCase::findActiveByOwnerAndSharedWith);
    }

    @Operation(
            summary = "List my deleted notes",
            description = "Soft-deleted notes the authenticated account owns, pinned first and then newest first.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "One page of notes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotePageViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid page or size",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @GetMapping("/deleted")
    public ResponseEntity<?> getDeleted(
            @Parameter(description = PAGE_DESCRIPTION) @RequestParam(required = false) Integer page,
            @Parameter(description = SIZE_DESCRIPTION) @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return listPage(authenticatedUser, null, null, page, size, useCase::findDeletedByOwner);
    }

    private ResponseEntity<?> listPage(
            AuthenticatedUser authenticatedUser,
            NoteStatus status,
            String sharedWith,
            Integer page,
            Integer size,
            Function<ListNotesRequest, Result<NotePageResponse>> query
    ) {
        ListNotesRequest input;
        try {
            input = new ListNotesRequest(authenticatedUser.accountId(), status, sharedWith, PageQuery.of(page, size));
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.presentPage(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        return httpResponsePresenter.toHttp(
                presenter.presentPage(query.apply(input)),
                notes -> {
                    log.info("Returning {} of {} notes", notes.items().size(), notes.totalItems());
                    return ResponseEntity.ok(notes);
                });
    }
}
