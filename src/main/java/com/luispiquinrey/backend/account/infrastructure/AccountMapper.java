package com.luispiquinrey.backend.account.infrastructure;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.AccountStatus;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.account.domain.Role;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Primary
@Component
public class AccountMapper {

    public Account toDomain(AccountDocument accountDocument) {
        Objects.requireNonNull(accountDocument, "accountDocument cannot be null");

        return new Account.AccountBuilder()
                .id(accountDocument.getId() == null ? null : new UserId(accountDocument.getId().toString()))
                .email(new Email(accountDocument.getEmail()))
                .encodedPassword(new EncodedPassword(accountDocument.getEncodedPassword()))
                .role(accountDocument.getRole() == null ? null : Role.valueOf(accountDocument.getRole()))
                .status(accountDocument.getAccountStatus() == null ? null : AccountStatus.valueOf(accountDocument.getAccountStatus()))
                .isVerified(accountDocument.isVerified())
                .createdAt(toAccountDate(accountDocument.getCreatedAt()))
                .lastLoginAt(toAccountDate(accountDocument.getLastLoginAt()))
                .build();
    }

    public AccountDocument toDocument(Account account) {
        Objects.requireNonNull(account, "account cannot be null");

        return new AccountDocument(
                toObjectId(account.id().id()),
                account.email().email(),
                account.encodedPassword().value(),
                account.role().name(),
                account.status().name(),
                account.isVerified(),
                account.createdAt() == null ? null : account.createdAt().value().toString(),
                account.lastLoginAt() == null ? null : account.lastLoginAt().value().toString()
        );
    }

    public ObjectId toObjectId(String id) {
        Objects.requireNonNull(id, "id cannot be null");
        return new ObjectId(id);
    }

    private Date toAccountDate(String date) {
        return date == null ? null : new Date(Instant.parse(date));
    }
}
