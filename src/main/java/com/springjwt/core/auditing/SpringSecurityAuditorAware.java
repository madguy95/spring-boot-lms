package com.springjwt.core.auditing;

import com.springjwt.common.util.AuthUtil;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AuditorAware implementation for JPA Auditing
 * Automatically captures the current user for @CreatedBy and @LastModifiedBy
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(AuthUtil.getCurrentUserId());
    }
}
