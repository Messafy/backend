package com.luispiquinrey.backend.account.infrastructure;

import com.luispiquinrey.backend.account.domain.AccountConflictException;
import com.luispiquinrey.backend.account.domain.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.AccountPasswordMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccountExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountExceptionHandler.class);

    @ExceptionHandler(AccountConflictException.class)
    public ProblemDetail handleAccountConflict(AccountConflictException ex) {
        log.warn("Request failed because the account state does not allow the operation: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AccountPasswordMismatchException.class)
    public ProblemDetail handlePasswordMismatch(AccountPasswordMismatchException ex) {
        log.warn("Request failed because credentials were invalid: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "invalid credentials");
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFoundById(AccountNotFoundException ex) {
        log.warn("Request failed because the account was not found: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleAccountNotFound(UsernameNotFoundException ex) {
        log.warn("Request failed because the account was not found: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "invalid credentials");
    }
}
