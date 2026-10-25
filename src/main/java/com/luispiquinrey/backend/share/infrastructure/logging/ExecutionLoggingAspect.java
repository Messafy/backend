package com.luispiquinrey.backend.share.infrastructure.logging;

import com.luispiquinrey.backend.share.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
class ExecutionLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(ExecutionLoggingAspect.class);

    @Around("execution(public * *(..)) && ("
            + "@within(com.luispiquinrey.backend.share.logging.LogExecution) || "
            + "@annotation(com.luispiquinrey.backend.share.logging.LogExecution))")
    public Object logExecution(ProceedingJoinPoint invocation) throws Throwable {
        if (!log.isDebugEnabled()) {
            return invocation.proceed();
        }

        String operation = invocation.getTarget().getClass().getSimpleName()
                + "." + invocation.getSignature().getName();
        log.debug("execution.start operation={}", operation);
        long started = System.nanoTime();
        String errorType = null;
        try {
            Object outcome = invocation.proceed();
            if (outcome instanceof Result<?> result && result.isFailure()) {
                errorType = "ApplicationError";
            }
            return outcome;
        } catch (Throwable failure) {
            errorType = failure.getClass().getSimpleName();
            throw failure;
        } finally {
            double durationMs = (System.nanoTime() - started) / 1_000_000.0;
            log.debug("execution.end operation={} outcome={} duration_ms={} error_type={}",
                    operation, errorType == null ? "success" : "failure", durationMs,
                    errorType == null ? "none" : errorType);
        }
    }
}
