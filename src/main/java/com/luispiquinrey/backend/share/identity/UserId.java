package com.luispiquinrey.backend.share.identity;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record UserId(String id) {

    public UserId {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id cannot be null or blank");
        }
    }
}
