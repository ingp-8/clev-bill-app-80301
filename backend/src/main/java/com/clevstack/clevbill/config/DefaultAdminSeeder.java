package com.clevstack.clevbill.config;

import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.RoleRepository;
import com.clevstack.clevbill.repository.UserRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a default admin user on first boot so there's a way to log in
 * before any user-management screen exists. Logs the generated credentials
 * once — change the password immediately in a real deployment.
 */
@Component
public class DefaultAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminSeeder.class);
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminSeeder(
            UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Role superAdmin = roleRepository
                .findByRoleName(Role.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Seeded role '" + Role.SUPER_ADMIN + "' is missing — check the V14 migration ran"));

        User admin = new User();
        admin.setUsername(DEFAULT_USERNAME);
        admin.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        admin.setFullName("Administrator");
        admin.setEnabled(true);
        admin.setRoles(Set.of(superAdmin));
        userRepository.save(admin);

        log.warn(
                "No users found — created default admin user (username: {}, password: {}). Change this password immediately.",
                DEFAULT_USERNAME,
                DEFAULT_PASSWORD);
    }
}
