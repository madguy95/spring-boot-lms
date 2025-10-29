package com.springjwt.module.user.domain.service;

import com.springjwt.common.enums.ERole;
import com.springjwt.module.user.domain.entity.Role;
import com.springjwt.module.user.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleDomainService {

    private final RoleRepository roleRepository;

    public Role findByName(ERole roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("role.not.found"));
    }
}
