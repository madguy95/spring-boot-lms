package com.springjwt.module.user.domain.service;

import com.springjwt.common.constant.UserFields;
import com.springjwt.common.exception.ValidationException;
import com.springjwt.module.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    public void existsByUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException(UserFields.USERNAME, "user.username.already.exists");
        }
    }

    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException(UserFields.EMAIL, "user.email.already.exists");
        }
    }
}
