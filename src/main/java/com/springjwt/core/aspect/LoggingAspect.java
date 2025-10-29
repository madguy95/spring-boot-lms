package com.springjwt.core.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP for logging method execution with parameters and execution time
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.springjwt.module..business..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.debug(">> {}.{}() - Args: {}",
                className,
                methodName,
                Arrays.toString(joinPoint.getArgs()));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("<< {}.{}() - Duration: {}ms", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("<< {}.{}() - Failed after {}ms - Error: {}",
                    className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.springjwt.module..domain.repository..*(..))")
    public Object logRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.trace("DB Query: {}.{}() - {}ms", className, methodName, duration);
            return result;
        } catch (Exception e) {
            log.error("DB Error: {}.{}() - {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
