package com.growlink.backend.security;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserRole;
import com.growlink.backend.entity.UserStatus;
import com.growlink.backend.repository.UserRepository;

@Component
public class SuperAdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.super-admin.name:}")
    private String superAdminName;

    @Value("${app.super-admin.email:}")
    private String superAdminEmail;

    @Value("${app.super-admin.password:}")
    private String superAdminPassword;

    public SuperAdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    SuperAdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder,
            String superAdminName, String superAdminEmail, String superAdminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.superAdminName = superAdminName;
        this.superAdminEmail = superAdminEmail;
        this.superAdminPassword = superAdminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        initialize();
    }

    public void initialize() {
        if (!hasRequiredBootstrapValues()) {
            log.info("Super admin bootstrap is disabled because no local environment values are configured.");
            return;
        }

        String normalizedEmail = normalizeEmail(superAdminEmail);
        String normalizedName = normalizeName(superAdminName);

        if (userRepository.existsByRole(UserRole.SUPER_ADMIN)) {
            log.info("A Super Admin already exists in the database. Bootstrap skipped.");
            return;
        }

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            log.warn("An account already exists with the configured super admin email. No existing account was modified.");
            return;
        }

        User user = new User();
        user.setFullName(normalizedName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(superAdminPassword));
        user.setLocation("System");
        user.setRole(UserRole.SUPER_ADMIN);
        user.setRequestedRole(null);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Created a secure Super Admin account for {}.", normalizedEmail);
    }

    private boolean hasRequiredBootstrapValues() {
        return isNotBlank(superAdminName) && isNotBlank(superAdminEmail) && isNotBlank(superAdminPassword);
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
