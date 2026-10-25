package com.luispiquinrey.backend.share.infrastructure.logging;

import com.luispiquinrey.backend.share.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class Slf4jLogger implements Logger {
    private final org.slf4j.Logger delegate = LoggerFactory.getLogger("com.luispiquinrey.backend.application");

    @Override
    public void debug(String message, Object... arguments) {
        delegate.debug(message, arguments);
    }

    @Override
    public void info(String message, Object... arguments) {
        delegate.info(message, arguments);
    }

    @Override
    public void warn(String message, Object... arguments) {
        delegate.warn(message, arguments);
    }

    @Override
    public void error(String message, Object... arguments) {
        delegate.error(message, arguments);
    }
}
