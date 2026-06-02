package com.springjwt.module.user.business;

import com.springjwt.module.user.domain.entity.User;
import com.springjwt.module.user.model.request.ChangePasswordRequest;
import com.springjwt.module.user.model.request.SignupRequest;

public interface UserService {
    User registerUser(SignupRequest signUpRequest);
    void changePassword(ChangePasswordRequest request);
}
