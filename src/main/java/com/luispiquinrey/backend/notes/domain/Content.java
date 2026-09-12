package com.luispiquinrey.backend.notes.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Content(String content) {
    public Content {
        content = switch (content) {
            case null -> throw new IllegalArgumentException("content cannot be null");

            case String value when value.isBlank() -> throw new IllegalArgumentException("content cannot be blank");
            case String value when value.trim().length() > 1000 -> throw new IllegalArgumentException("content cannot be longer than 1000 characters");
            case String value when value.matches(".*[A-Za-z][A-Za-z0-9+.-]*://.*") -> throw new IllegalArgumentException("content cannot contain URLs");
            case String value when value.chars()
                    .anyMatch(character -> Character.isISOControl(
                            character)
                            && character != '\n'
                            && character != '\t'
                            && character != '\r')
                    -> throw new IllegalArgumentException("content cannot contain control characters");
            case String value when value.lines().count() > 300
                    -> throw new IllegalArgumentException("content cannot contain more than 300 lines");
            default -> content.trim();
        };
    }
}
