package com.growlink.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByRole(UserRole role);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(UserRole role);
}
