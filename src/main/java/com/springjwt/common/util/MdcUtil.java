package com.springjwt.common.util;

import com.springjwt.common.constant.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Utility class for MDC (Mapped Diagnostic Context) operations
 * Helps with propagating context to async operations
 */
@Slf4j
public class MdcUtil {

    private MdcUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get current MDC context map
     */
    public static Map<String, String> getContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Get current Correlation ID
     */
    public static String getCorrelationId() {
        return MDC.get(LogConstants.CORRELATION_ID_MDC_KEY);
    }

    /**
     * Get current User ID
     */
    public static String getUserId() {
        return MDC.get(LogConstants.USER_ID_MDC_KEY);
    }

    /**
     * Set MDC context map
     */
    public static void setContextMap(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }

    /**
     * Clear MDC context
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Wrap Runnable with MDC context propagation
     */
    public static Runnable wrap(Runnable runnable) {
        Map<String, String> contextMap = getContextMap();
        return () -> {
            try {
                setContextMap(contextMap);
                runnable.run();
            } finally {
                clear();
            }
        };
    }

    /**
     * Wrap Callable with MDC context propagation
     */
    public static <T> Callable<T> wrap(Callable<T> callable) {
        Map<String, String> contextMap = getContextMap();
        return () -> {
            try {
                setContextMap(contextMap);
                return callable.call();
            } finally {
                clear();
            }
        };
    }

    /**
     * Execute runnable with current MDC context
     */
    public static void executeWithContext(Runnable runnable) {
        Map<String, String> contextMap = getContextMap();
        try {
            setContextMap(contextMap);
            runnable.run();
        } finally {
            clear();
        }
    }

    /**
     * Log current MDC context (for debugging)
     */
    public static void logContext() {
        Map<String, String> contextMap = getContextMap();
        if (contextMap != null && !contextMap.isEmpty()) {
            log.debug("Current MDC Context: {}", contextMap);
        } else {
            log.debug("MDC Context is empty");
        }
    }
}

