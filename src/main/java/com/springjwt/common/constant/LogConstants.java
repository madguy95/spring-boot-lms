package com.springjwt.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogConstants {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_URI_MDC_KEY = "requestUri";
    public static final String REQUEST_METHOD_MDC_KEY = "requestMethod";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String REQUEST_IP_MDC_KEY = "requestIp";
}
