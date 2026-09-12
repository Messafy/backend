package com.luispiquinrey.backend.notes.slices.create.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class NoInvalidControlCharactersValidator
        implements ConstraintValidator<NoInvalidControlCharacters, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.codePoints().noneMatch(character ->
                Character.isISOControl(character)
                        && character != '\n'
                        && character != '\r'
                        && character != '\t'
        );
    }
}
