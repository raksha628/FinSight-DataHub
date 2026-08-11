package com.finsight.datahub.config;

import com.finsight.datahub.entity.User;
import com.finsight.datahub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           PlatformTransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(String... args) {
        transactionTemplate.executeWithoutResult(status -> {
            log.info("DataInitializer: Verifying default system user accounts...");

            seedUser("admin", "admin@finsight.com", "Admin@123", User.Role.ADMIN);
            seedUser("analyst", "analyst@finsight.com", "Analyst@123", User.Role.ANALYST);
            seedUser("viewer", "viewer@finsight.com", "Viewer@123", User.Role.VIEWER);

            log.info("DataInitializer: Default user accounts verified successfully.");
        });
    }

    private void seedUser(String username, String email, String rawPassword, User.Role role) {
        String encodedPassword = passwordEncoder.encode(rawPassword);

        if (userRepository.existsByUsername(username)) {
            int rows = userRepository.updatePasswordByUsername(username, encodedPassword);
            log.info("DataInitializer: Updated existing account '{}' password hash directly in PostgreSQL (rows: {}).", username, rows);
        } else {
            User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .password(encodedPassword)
                    .role(role)
                    .isActive(true)
                    .build();
            userRepository.save(newUser);
            log.info("DataInitializer: Created new account '{}' with role {}.", username, role);
        }
    }
}
