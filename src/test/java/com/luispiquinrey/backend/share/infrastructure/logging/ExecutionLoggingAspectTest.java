package com.luispiquinrey.backend.share.infrastructure.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExecutionLoggingAspectTest {
    @Test
    @Tag("executionLogging")
    @Timeout(1)
    void resultFailureIsNotReportedAsSuccessAndDoesNotLeakItsDetails() throws Throwable {
        var invocation = mock(ProceedingJoinPoint.class);
        var signature = mock(Signature.class);
        when(invocation.getTarget()).thenReturn(this);
        when(invocation.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("operation");
        var failure = Result.failure(ApplicationError.notFoundError("sensitive diagnostic"));
        when(invocation.proceed()).thenReturn(failure);
        Logger logger = (Logger) LoggerFactory.getLogger(ExecutionLoggingAspect.class);
        Level previous = logger.getLevel();
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
        try {
            assertSame(failure, new ExecutionLoggingAspect().logExecution(invocation));
            var logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
            assertTrue(logs.stream().anyMatch(line -> line.contains("outcome=failure")));
            assertFalse(logs.stream().anyMatch(line -> line.contains("outcome=success") || line.contains("sensitive diagnostic")));

            appender.list.clear();
            var success = Result.success("sensitive result");
            when(invocation.proceed()).thenReturn(success);
            assertSame(success, new ExecutionLoggingAspect().logExecution(invocation));
            assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("outcome=success")));
            assertFalse(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("sensitive result")));

            appender.list.clear();
            var exception = new IllegalStateException("sensitive exception");
            when(invocation.proceed()).thenThrow(exception);
            assertSame(exception, assertThrows(IllegalStateException.class,
                    () -> new ExecutionLoggingAspect().logExecution(invocation)));
            assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("outcome=failure")));
            assertFalse(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("sensitive exception")));
        } finally {
            logger.setLevel(previous);
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
