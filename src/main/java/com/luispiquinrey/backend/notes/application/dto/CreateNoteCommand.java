package com.luispiquinrey.backend.notes.application.dto;

import java.util.List;

public record CreateNoteCommand(NoteType type, String title, String content, String sharedWith, List<String> tags) {
    public enum NoteType { PRIVATE, SHARED }
}
