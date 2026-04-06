package com.finance.config;

import com.finance.entity.User;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.findAll()
                .stream()
                .anyMatch(u -> u.getRole() == User.Role.ADMIN);

        if (!adminExists) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@finance.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .status(User.UserStatus.ACTIVE)
                    .build());
            log.info("Default admin created — email: admin@finance.com  password: admin123");
        }
    }
}
