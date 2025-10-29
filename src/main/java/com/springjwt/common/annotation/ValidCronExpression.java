package com.springjwt.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation for Quartz cron expressions.
 * Uses Quartz's CronExpression.validateExpression() for validation.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CronExpressionValidator.class)
@Documented
public @interface ValidCronExpression {

    String message() default "{scheduler.cron.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
