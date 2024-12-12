package org.example.logginglibrary.logginglibrary;

import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLogger {

    private static final Logger logger = LoggerFactory.getLogger(CustomLogger.class);

    public static void info(String message) {
        //enrichLogWithTracingInfo();
        logger.info(message);
    }

    public static void warn(String message) {
        //enrichLogWithTracingInfo();
        logger.warn(message);
    }

    public static void error(String message, Throwable throwable) {
        //enrichLogWithTracingInfo();
        logger.error(message, throwable);
    }

    private static void enrichLogWithTracingInfo() {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            MDC.put("traceId", currentSpan.getSpanContext().getTraceId());
            MDC.put("spanId", currentSpan.getSpanContext().getSpanId());
        }
    }
}