package com.springjwt.common.annotation;

import com.springjwt.common.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited
 * Usage: @Auditable(action = AuditAction.CREATE, entityType = "User")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The action being performed (CREATE, UPDATE, DELETE, etc.)
     */
    AuditAction action();

    /**
     * The type of entity being acted upon (e.g., "User", "File", "Order")
     */
    String entityType();

    /**
     * Description of the action (optional)
     */
    String description() default "";

    /**
     * Parameter name that contains the entity ID (optional)
     * If specified, will extract ID from this parameter
     * Example: @Auditable(action = UPDATE, entityType = "User", entityIdParam = "id")
     */
    String entityIdParam() default "";
}
