package com.luispiquinrey.backend.notes.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import com.luispiquinrey.backend.share.identity.UserId;

@ValueObject
public record SharedWith(UserId ownerId) {

    public SharedWith {
        if (ownerId == null) {
            throw new IllegalArgumentException("sharedWith cannot be null");
        }
    }

    public SharedWith(String ownerId) {
        this(new UserId(ownerId));
    }
}
