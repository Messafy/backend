package com.luispiquinrey.backend.notes.slices.create.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public final class NoUrlsValidator implements ConstraintValidator<NoUrls, String> {

    private static final Pattern URL_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9+.-]*://");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || !URL_PATTERN.matcher(value).find();
    }
}
