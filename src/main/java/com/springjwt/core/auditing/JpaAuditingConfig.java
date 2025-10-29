package com.springjwt.core.auditing;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enable JPA Auditing
 * This allows @CreatedBy, @LastModifiedBy, @CreatedDate, @LastModifiedDate to work automatically
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class JpaAuditingConfig {
}

