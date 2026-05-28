package com.springjwt.module.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springjwt.module.user.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	Optional<User> findByPhone(String phone);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

	Boolean existsByPhone(String phone);

	Boolean existsByEmailAndIdNot(String email, Long id);

	Boolean existsByPhoneAndIdNot(String phone, Long id);
}
