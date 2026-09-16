package com.luispiquinrey.backend.notes.domain;

public record Tag(String tag) {
    public Tag {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("tag cannot be blank or null");
        }

        tag = tag.strip();

        if (!tag.matches("[\\p{L}\\p{M}\\p{N} _-]+")) {
            throw new IllegalArgumentException(
                    "tag can only contain letters, numbers, spaces, hyphens and underscores"
            );
        }
    }
}
