package com.luispiquinrey.backend.account.infrastructure.persistence;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.luispiquinrey.backend.account.application.port.PublicCodeGenerator;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import org.springframework.stereotype.Component;

@Component
public class NanoIdPublicCodeGenerator implements PublicCodeGenerator {

    @Override
    public String nextCode() {
        return NanoIdUtils.randomNanoId(
                NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                NanoIdUtils.DEFAULT_ALPHABET,
                PublicCode.LENGTH
        );
    }
}