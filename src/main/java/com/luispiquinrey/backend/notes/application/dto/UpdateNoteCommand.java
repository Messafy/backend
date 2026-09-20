package com.luispiquinrey.backend.notes.application.dto;

import java.util.List;

public record UpdateNoteCommand(String title, String content, List<String> tags) { }
