package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.application.dto.CreatePrivateNoteCommand;
import com.luispiquinrey.backend.notes.presentation.validation.MaxLines;
import com.luispiquinrey.backend.notes.presentation.validation.NoInvalidControlCharacters;
import com.luispiquinrey.backend.notes.presentation.validation.NoUrls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PrivateNoteCreationRequest(
        @NotNull(message = "Title cannot be null")
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 100, message = "Title cannot be longer than 100 characters")
        @Pattern(
                regexp = "^[^\\r\\n]*$",
                message = "Title must not contain line breaks"
        ) String title,
        @NotNull(message = "Content cannot be null")
        @NotBlank(message = "Content cannot be blank")
        @Size(max = 1000, message = "Content cannot be longer than 1000 characters")
        @MaxLines(value = 300, message = "Content cannot contain more than 300 lines")
        @NoUrls(message = "Content cannot contain URLs")
        @NoInvalidControlCharacters(message = "Content cannot contain invalid control characters")
        String content,
        List<String> tags
) {
    public CreatePrivateNoteCommand toCommand() {
        return new CreatePrivateNoteCommand(title, content, tags);
    }
}