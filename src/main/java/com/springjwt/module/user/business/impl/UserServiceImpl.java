package com.springjwt.module.user.business.impl;

import com.springjwt.common.annotation.Auditable;
import com.springjwt.common.enums.AuditAction;
import com.springjwt.module.user.business.UserService;
import com.springjwt.common.enums.ERole;
import com.springjwt.module.user.domain.entity.Role;
import com.springjwt.module.user.domain.entity.User;
import com.springjwt.module.user.domain.repository.UserRepository;
import com.springjwt.module.user.domain.service.RoleDomainService;
import com.springjwt.module.user.domain.service.UserDomainService;
import com.springjwt.module.user.model.request.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleDomainService roleDomainService;
    private final UserDomainService userDomainService;
    private final PasswordEncoder encoder;

    @Transactional
    @Auditable(action = AuditAction.CREATE, entityType = "", entityIdParam = "id", description = "Register new user")
    public User registerUser(SignupRequest signUpRequest) {
        userDomainService.existsByEmail(signUpRequest.getEmail());
        userDomainService.existsByUsername(signUpRequest.getUsername());
        userDomainService.existsByPhone(signUpRequest.getPhone());
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                signUpRequest.getPhone(),
                encoder.encode(signUpRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleDomainService.findByName(ERole.ROLE_PARENT);
        roles.add(userRole);

        user.setRoles(roles);
        return userRepository.save(user);
    }
}
