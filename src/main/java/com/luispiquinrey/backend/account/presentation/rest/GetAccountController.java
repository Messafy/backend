package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.request.GetAccountRequest;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import com.luispiquinrey.backend.account.application.use_cases.GetAccountUseCase;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import com.luispiquinrey.backend.account.presentation.viewmodel.AccountViewModel;
import com.luispiquinrey.backend.account.presentation.presenter.GetAccountPresenter;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
@Tag(name = "Accounts", description = "Account profile lookup for the authenticated user and for admins.")
@SecurityRequirement(name = "bearerAuth")
public class GetAccountController {

    private static final Logger log = LoggerFactory.getLogger(GetAccountController.class);

    private final GetAccountUseCase getAccountUseCase;
    private final GetAccountPresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public GetAccountController(
            GetAccountUseCase getAccountUseCase,
            GetAccountPresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.getAccountUseCase = getAccountUseCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
    }

    @Operation(
            summary = "Get the current account",
            description = "Returns the profile (id, email, public code, verification state) of the account "
                    + "identified by the JWT. Requires USER or ADMIN role.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current account found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated without USER or ADMIN role",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account no longer exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        log.info("Received request to get current account");
        GetAccountRequest input;
        try {
            input = new GetAccountRequest(principal.accountId());
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        return httpResponsePresenter.toHttp(presenter.present(getAccountUseCase.getMyAccount(input)));
    }

    @Operation(
            summary = "Get an account by its id",
            description = "Admin-only lookup of any account profile by its MongoDB ObjectId string.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed account id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid or expired JWT",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated without ADMIN role",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "404",
                    description = "No account exists with the given id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Account id as MongoDB ObjectId string", example = "507f1f77bcf86cd799439011")
            @PathVariable String id) {
        log.info("Received request to get account by id");
        GetAccountRequest input;
        try {
            input = new GetAccountRequest(id);
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        return httpResponsePresenter.toHttp(presenter.present(getAccountUseCase.findById(input)));
    }
}
