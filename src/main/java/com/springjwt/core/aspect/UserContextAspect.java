package com.springjwt.core.aspect;

import com.springjwt.common.constant.LogConstants;
import com.springjwt.common.util.AuthUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * AOP to add User ID to MDC for authenticated requests
 */
@Aspect
@Component
public class UserContextAspect {

    @Around("execution(* com.springjwt.module..api..*(..))")
    public Object addUserContext(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            String userId = AuthUtil.getCurrentUserId();
            MDC.put(LogConstants.USER_ID_MDC_KEY, userId);

            return joinPoint.proceed();
        } finally {
            MDC.remove(LogConstants.USER_ID_MDC_KEY);
        }
    }
}

