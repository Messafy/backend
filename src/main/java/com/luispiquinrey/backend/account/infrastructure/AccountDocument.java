package com.luispiquinrey.backend.account.infrastructure;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
public class AccountDocument {
    @Id
    private ObjectId id;

    @Version
    private Integer version;

    @Indexed(name = "email", unique = true)
    private String email;

    private String encodedPassword;
    private String role;
    private String accountStatus;
    private boolean isVerified;
    private String createdAt;
    private String lastLoginAt;

    public AccountDocument() {}

    public AccountDocument(ObjectId id, String email, String encodedPassword, String role, String accountStatus, boolean isVerified, String createdAt, String lastLoginAt) {
        this.id = id;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.role = role;
        this.accountStatus = accountStatus;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
