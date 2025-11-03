package com.example.coffeemanager.config;

import com.example.coffeemanager.entity.Account;
import com.example.coffeemanager.entity.Role;
import com.example.coffeemanager.repository.AccountRepository;
import com.example.coffeemanager.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    static final String ADMIN_USER_NAME = "admin";
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(
            AccountRepository accountRepository,
            RoleRepository roleRepository) { // 👈 CHỈ CẦN 2 Repository này

        log.info("Initializing application.....");

        return args -> {
            // --- 1. Tạo các vai trò (Roles) nếu chúng chưa tồn tại ---
            log.info("Checking and creating default roles...");
            Role staffRole = createRoleIfNotFound(roleRepository, "STAFF", "Nhân viên cơ bản");
            Role managerRole = createRoleIfNotFound(roleRepository, "MANAGER", "Quản lý cửa hàng");
            Role adminRole = createRoleIfNotFound(roleRepository, "ADMIN", "Quản trị hệ thống");

            // --- 2. Tạo tài khoản Admin mặc định nếu chưa có ---
            if (accountRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                log.info("Admin user not found. Creating admin account...");

                // a. Bỏ qua phần tạo Staff

                // b. Tạo tài khoản Account cho admin
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole); // Gán vai trò ADMIN

                Account adminAccount = Account.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .enabled(true)
                        .roles(roles)
                        .staffId("ADMIN_SYSTEM") // 👈 Gán một ID đặc biệt
                        .build();

                accountRepository.save(adminAccount);
                log.warn("Admin user ('admin') has been created with default password: 'admin'.");
            }
            log.info("Application initialization completed .....");
        };
    }

    /**
     * Hàm helper để kiểm tra và tạo Role nếu chưa tồn tại
     */
    private Role createRoleIfNotFound(RoleRepository roleRepository, String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    log.info("Creating role: {}", name);
                    return roleRepository.save(Role.builder()
                            .name(name)
                            .description(description)
                            .build());
                });
    }
}