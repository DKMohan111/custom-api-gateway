
package com.apigateway.config;

import com.apigateway.entity.User;
import com.apigateway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.password}") String adminPassword) {

        return args -> {
            User admin = userRepository.findByUsername("admin")
                    .orElse(null);

            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setRole("ADMIN");
            }

            if (!passwordEncoder.matches(
                    adminPassword, admin.getPassword() == null ? "" : admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(adminPassword));
                userRepository.save(admin);
            }

        };
    }
}
