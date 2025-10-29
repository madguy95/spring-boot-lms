package com.springjwt.core.logging;

import com.springjwt.common.constant.LogConstants;
import com.springjwt.common.util.AuthUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add Correlation ID to every request
 * This allows tracking all logs related to a single request
 */
@Component
@Order(1)
@Slf4j
public class CorrelationIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Get or generate correlation ID
            String correlationId = httpRequest.getHeader(LogConstants.CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(LogConstants.USER_ID_MDC_KEY, "anonymous");
            // Add to MDC (Mapped Diagnostic Context)
            MDC.put(LogConstants.CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put(LogConstants.REQUEST_URI_MDC_KEY, httpRequest.getRequestURI());
            MDC.put(LogConstants.REQUEST_METHOD_MDC_KEY, httpRequest.getMethod());
            MDC.put(LogConstants.REQUEST_IP_MDC_KEY, AuthUtil.getClientIP(httpRequest));

            // Add to response header so client can use it
            httpResponse.setHeader(LogConstants.CORRELATION_ID_HEADER, correlationId);

            log.info("Request started: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

            long startTime = System.currentTimeMillis();
            chain.doFilter(request, response);
            long duration = System.currentTimeMillis() - startTime;

            log.info("Request completed: {} {} - Status: {} - Duration: {}ms",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration);

        } finally {
            // Always clear MDC after request
            MDC.clear();
        }
    }
}

