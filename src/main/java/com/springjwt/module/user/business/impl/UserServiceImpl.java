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
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleDomainService.findByName(ERole.ROLE_USER);
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                ERole roleEnum = switch (role.toLowerCase()) {
                    case "admin" -> ERole.ROLE_ADMIN;
                    case "mod", "moderator" -> ERole.ROLE_MODERATOR;
                    default -> ERole.ROLE_USER;
                };
                Role foundRole = roleDomainService.findByName(roleEnum);
                roles.add(foundRole);
            });
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }
}
