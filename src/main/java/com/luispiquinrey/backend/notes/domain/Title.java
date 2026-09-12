package com.luispiquinrey.backend.notes.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Title(String title) {
    public Title {
        title = switch (title) {
            case null -> throw new IllegalArgumentException("title cannot be null or blank");
            case String value when value.isBlank() -> throw new IllegalArgumentException("title cannot be null or blank");
            case String value when value.length() > 100 -> throw new IllegalArgumentException("title cannot be longer than 100 characters");
            case String value when value.contains("\n") -> throw new IllegalArgumentException("title cannot contain new lines");
            default -> title;
        };
    }
}
