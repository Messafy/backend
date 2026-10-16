package com.luispiquinrey.backend.notes.presentation.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteTextValidatorsTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Plain text", "Unicode: ñ 😀", "Line one\nLine two\r\n\tIndented"})
    void acceptsTextWithOrdinaryWhitespace(String text) {
        assertTrue(new NoInvalidControlCharactersValidator().isValid(text, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Text\u0000", "Text\u0008", "Text\u001b", "Text\u007f", "Text\u0085"})
    void rejectsControlCharacters(String text) {
        assertFalse(new NoInvalidControlCharactersValidator().isValid(text, null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Plain text", "example.com", "user@example.com"})
    void acceptsTextWithoutUrlScheme(String text) {
        assertTrue(new NoUrlsValidator().isValid(text, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com", "Text HTTP://example.com", "ftp://files", "custom+app://path"})
    void rejectsUrlsIncludingCustomSchemes(String text) {
        assertFalse(new NoUrlsValidator().isValid(text, null));
    }

    @Test
    void enforcesConfiguredLineLimit() {
        MaxLines constraint = mock(MaxLines.class);
        when(constraint.value()).thenReturn(2);
        MaxLinesValidator validator = new MaxLinesValidator();
        validator.initialize(constraint);

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("one", null));
        assertTrue(validator.isValid("one\ntwo", null));
        assertTrue(validator.isValid("one\r\ntwo", null));
        assertFalse(validator.isValid("one\ntwo\nthree", null));
        assertFalse(validator.isValid("one\r\ntwo\r\nthree", null));
    }
}
