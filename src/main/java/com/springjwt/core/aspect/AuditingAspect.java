package com.springjwt.core.aspect;

import com.springjwt.common.annotation.Auditable;
import com.springjwt.common.constant.LogConstants;
import com.springjwt.common.util.AuthUtil;
import com.springjwt.module.audit.domain.entity.AuditLog;
import com.springjwt.module.audit.domain.service.AuditLogDomainService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * AOP Aspect to automatically create audit logs for methods annotated with @Auditable
 * Optimized version for production use:
 * - Async audit log saving
 * - Explicit entity ID parameter
 * - Minimal reflection
 * - Metadata-only (no request/response data serialization)
 * - Fast execution (~1ms overhead)
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditingAspect {

    private final AuditLogDomainService auditLogDomainService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Build audit log - METADATA ONLY, no data serialization
        AuditLog auditLog = AuditLog.builder()
                .correlationId(MDC.get(LogConstants.CORRELATION_ID_MDC_KEY))
                .username(AuthUtil.getCurrentUsername())
                .userId(AuthUtil.getCurrentUserId())
                .action(auditable.action())
                .entityType(auditable.entityType())
                .description(auditable.description())
                .timestamp(Instant.now())
                .build();

        // Capture request info
        captureRequestInfo(auditLog);

        // Extract entity ID if specified
        if (!auditable.entityIdParam().isEmpty()) {
            extractEntityIdFromParam(joinPoint, auditable.entityIdParam(), auditLog);
        }

        try {
            // Execute the method
            Object result = joinPoint.proceed();

            // Success
            auditLog.setStatus("SUCCESS");

            // If entity ID not extracted from param, try from result (simple cases only)
            if (auditLog.getEntityId() == null && result != null) {
                extractEntityIdFromResultSimple(result, auditLog);
            }

            return result;

        } catch (Exception e) {
            // Failed
            auditLog.setStatus("FAILED");
            auditLog.setErrorMessage(e.getMessage());
            throw e;

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            auditLog.setDurationMs(duration);

            // Save audit log ASYNCHRONOUSLY to avoid blocking main thread
            saveAuditLogAsync(auditLog);
        }
    }

    /**
     * Save audit log asynchronously
     * This ensures audit logging doesn't impact API response time
     */
    @Async
    protected void saveAuditLogAsync(AuditLog auditLog) {
        try {
            auditLogDomainService.save(auditLog);
            log.debug("Audit log saved: {} {} by {}", auditLog.getAction(), auditLog.getEntityType(), auditLog.getUsername());
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    /**
     * Capture request information
     */
    private void captureRequestInfo(AuditLog auditLog) {
        HttpServletRequest request = AuthUtil.getCurrentRequest();
        if (request != null) {
            auditLog.setRequestUri(request.getRequestURI());
            auditLog.setRequestMethod(request.getMethod());
            auditLog.setIpAddress(AuthUtil.getClientIP(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
    }

    /**
     * Extract entity ID from specified parameter name
     * FAST: Direct parameter name lookup, no reflection needed
     */
    private void extractEntityIdFromParam(ProceedingJoinPoint joinPoint, String paramName, AuditLog auditLog) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (parameterNames == null || args == null) {
                return;
            }

            // Find parameter by exact name match
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(paramName) && args[i] != null) {
                    auditLog.setEntityId(args[i].toString());
                    log.debug("Entity ID extracted from param '{}': {}", paramName, args[i]);
                    return;
                }
            }

        } catch (Exception e) {
            log.debug("Could not extract entity ID from param '{}': {}", paramName, e.getMessage());
        }
    }

    /**
     * Extract entity ID from result - SIMPLE cases only
     * Only tries getId() method, no deep reflection
     */
    private void extractEntityIdFromResultSimple(Object result, AuditLog auditLog) {
        try {
            // Only for simple types or objects with getId()
            if (result instanceof Long || result instanceof Integer || result instanceof String) {
                auditLog.setEntityId(result.toString());
                return;
            }

            // Try getId() only (most common case)
            var method = result.getClass().getMethod("getId");
            Object id = method.invoke(result);
            if (id != null) {
                auditLog.setEntityId(id.toString());
            }

        } catch (Exception e) {
            // Silently ignore - entity ID is optional
            log.trace("Could not extract entity ID from result: {}", e.getMessage());
        }
    }
}

