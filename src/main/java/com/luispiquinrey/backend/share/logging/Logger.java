package com.luispiquinrey.backend.share.logging;

public interface Logger {
    void debug(String message, Object... arguments);
    void info(String message, Object... arguments);
    void warn(String message, Object... arguments);
    void error(String message, Object... arguments);
}
