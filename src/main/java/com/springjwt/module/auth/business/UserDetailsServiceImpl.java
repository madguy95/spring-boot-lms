package com.springjwt.module.auth.business;

import com.springjwt.module.auth.model.dto.UserPrincipal;
import com.springjwt.module.user.domain.entity.User;
import com.springjwt.module.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with phone: " + loginId));

        return UserPrincipal.build(user);
    }

}
