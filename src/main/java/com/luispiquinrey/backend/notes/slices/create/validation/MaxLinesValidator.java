package com.luispiquinrey.backend.notes.slices.create.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class MaxLinesValidator implements ConstraintValidator<MaxLines, String> {

    private int maximum;

    @Override
    public void initialize(MaxLines constraint) {
        maximum = constraint.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.lines().count() <= maximum;
    }
}
