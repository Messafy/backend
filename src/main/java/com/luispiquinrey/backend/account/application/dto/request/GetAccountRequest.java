package com.luispiquinrey.backend.account.application.dto.request;

import com.luispiquinrey.backend.share.identity.UserId;

public record GetAccountRequest(String id) {
    public GetAccountRequest {
        new UserId(id);
    }

    public UserId toUserId() {
        return new UserId(id);
    }
}
