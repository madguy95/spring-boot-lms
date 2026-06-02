package com.springjwt.module.user.business.impl;

import com.springjwt.common.annotation.Auditable;
import com.springjwt.common.enums.AuditAction;
import com.springjwt.common.exception.BadRequestException;
import com.springjwt.common.util.AuthUtil;
import com.springjwt.module.user.business.UserService;
import com.springjwt.common.enums.ERole;
import com.springjwt.module.user.domain.entity.Role;
import com.springjwt.module.user.domain.entity.User;
import com.springjwt.module.user.domain.repository.UserRepository;
import com.springjwt.module.user.domain.service.RoleDomainService;
import com.springjwt.module.user.domain.service.UserDomainService;
import com.springjwt.module.user.model.request.ChangePasswordRequest;
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

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityType = "User", entityIdParam = "id", description = "Change password")
    public void changePassword(ChangePasswordRequest request) {
        Long userId = Long.parseLong(AuthUtil.getCurrentUserId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("user.not.found"));

        if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("user.password.incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("user.password.confirm.mismatch");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
