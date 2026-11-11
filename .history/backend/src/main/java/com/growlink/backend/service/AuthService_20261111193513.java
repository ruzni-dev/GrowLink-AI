package com.growlink.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.growlink.backend.dto.AuthResponse;
import com.growlink.backend.dto.LoginRequest;
import com.growlink.backend.dto.RegisterRequest;
import com.growlink.backend.dto.UserResponse;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserRole;
import com.growlink.backend.entity.UserStatus;
import com.growlink.backend.exception.DuplicateEmailException;
import com.growlink.backend.repository.UserRepository;
import com.growlink.backend.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        UserRole requestedRole = normalizeRequestedRole(request.role());
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(blankToNull(request.phone()));
        user.setLocation(request.location().trim());
        user.setRole(UserRole.USER);
        user.setRequestedRole(requestedRole == UserRole.USER ? null : requestedRole);
        user.setStatus(requestedRole == UserRole.USER ? UserStatus.ACTIVE : UserStatus.PENDING_APPROVAL);

        return UserResponse.from(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername()).orElseThrow();
        return new AuthResponse(jwtService.generateToken(principal.getUsername()), "Bearer", jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    public UserResponse currentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }

    public void logout() {
        // JWT access tokens are stateless. The client must discard its in-memory token.
    }

    private UserRole normalizeRequestedRole(UserRole role) {
        if (role == null || role == UserRole.ADMIN) {
            return UserRole.USER;
        }
        return role;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
