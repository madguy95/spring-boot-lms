package com.springjwt.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.quartz.CronExpression;

import java.text.ParseException;

/**
 * Validator for Quartz cron expressions.
 * Uses Quartz's CronExpression.validateExpression() method.
 */
public class CronExpressionValidator implements ConstraintValidator<ValidCronExpression, String> {

    @Override
    public void initialize(ValidCronExpression constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String cronExpression, ConstraintValidatorContext context) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        try {
            CronExpression.validateExpression(cronExpression);
            return true;
        } catch (ParseException e) {
            // Invalid cron expression
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid cron expression: " + e.getMessage()
            ).addConstraintViolation();
            return false;
        }
    }
}

