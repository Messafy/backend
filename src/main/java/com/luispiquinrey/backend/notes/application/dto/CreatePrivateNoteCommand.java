package com.luispiquinrey.backend.notes.application.dto;

import java.util.List;

public record CreatePrivateNoteCommand(String title, String content, List<String> tags) {
}