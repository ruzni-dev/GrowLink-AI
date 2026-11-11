package com.growlink.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserRole;
import com.growlink.backend.repository.UserRepository;

class SuperAdminBootstrapTest {

    @Test
    void createsSuperAdminWhenMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        when(userRepository.existsByRole(UserRole.SUPER_ADMIN)).thenReturn(false);
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuperAdminBootstrap bootstrap = new SuperAdminBootstrap(
                userRepository,
                passwordEncoder,
                "Bootstrap Admin",
                "admin@example.com",
                "StrongPass1");

        bootstrap.initialize();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(UserRole.SUPER_ADMIN, saved.getRole());
        assertEquals("Bootstrap Admin", saved.getFullName());
        assertTrue(passwordEncoder.matches("StrongPass1", saved.getPassword()));
    }

    @Test
    void doesNotOverwriteExistingSuperAdmin() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        when(userRepository.existsByRole(UserRole.SUPER_ADMIN)).thenReturn(true);

        SuperAdminBootstrap bootstrap = new SuperAdminBootstrap(
                userRepository,
                passwordEncoder,
                "Bootstrap Admin",
                "admin@example.com",
                "StrongPass1");

        bootstrap.initialize();

        verify(userRepository).existsByRole(UserRole.SUPER_ADMIN);
    }
}
