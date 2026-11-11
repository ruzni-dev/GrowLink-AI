package com.growlink.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.growlink.backend.dto.LoginRequest;
import com.growlink.backend.dto.RegisterRequest;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserRole;
import com.growlink.backend.exception.DuplicateEmailException;
import com.growlink.backend.repository.UserRepository;
import com.growlink.backend.security.JwtService;

class AuthServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService(
            "VGhpcy1pcy1hLXRlc3Qta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2", 900);

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void registerHashesPasswordAndDefaultsToUserRole() {
        RegisterRequest request = new RegisterRequest("Amina Rahman", "AMINA@example.com", "StrongPass1", null, "Colombo", UserRole.ADMIN);
        when(userRepository.existsByEmailIgnoreCase("amina@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertNotEquals("StrongPass1", saved.getPassword());
        assertTrue(passwordEncoder.matches("StrongPass1", saved.getPassword()));
        assertTrue(saved.getRole() == UserRole.USER);
    }

    @Test
    void duplicateEmailIsRejected() {
        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(
                new RegisterRequest("Existing User", "existing@example.com", "StrongPass1", null, "Accra", UserRole.USER)));
    }

    @Test
    void loginReturnsJwtAndSafeUser() {
        User user = user("person@example.com", passwordEncoder.encode("StrongPass1"));
        UserDetails details = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword()).roles("USER").build();
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(details);
        when(userRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));

        var response = authService.login(new LoginRequest("PERSON@example.com", "StrongPass1"));

        assertTrue(response.accessToken().split("\\.").length == 3);
        assertTrue(response.user().email().equals("person@example.com"));
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void invalidPasswordIsRejected() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(new LoginRequest("person@example.com", "wrong")));
    }

    private User user(String email, String password) {
        User user = new User();
        user.setFullName("Test Person");
        user.setEmail(email);
        user.setPassword(password);
        user.setLocation("Colombo");
        user.setRole(UserRole.USER);
        return user;
    }
}
