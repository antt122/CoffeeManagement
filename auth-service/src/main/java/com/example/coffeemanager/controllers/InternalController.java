package com.example.coffeemanager.controllers;


import com.example.coffeemanager.DTO.request.InternalAccountRequest;
import com.example.coffeemanager.DTO.response.ApiResponse;
import com.example.coffeemanager.DTO.response.InternalAccountResponse;
import com.example.coffeemanager.entity.Account;
import com.example.coffeemanager.entity.Role;
import com.example.coffeemanager.repository.AccountRepository;
import com.example.coffeemanager.repository.RoleRepository;
import com.example.coffeemanager.service.UsernameGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalController {

    private final UsernameGenerationService usernameGenerationService;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "123"; // Mật khẩu mặc định

    /**
     * API nội bộ (chỉ cho hr-service gọi)
     * Tự động tạo username/staffId (1000*), tạo password mặc định.
     */
    @PostMapping("/create-account")
    @Transactional
    public ApiResponse<InternalAccountResponse> createInternalAccount(
            @RequestBody InternalAccountRequest request) {

        // 1. Tạo username mới (ví dụ: "10001")
        String newUsername = usernameGenerationService.generateNextUsername();
        log.info("Generating new account ID/Username: {}", newUsername);

        // 2. Tìm Role
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));

        // 3. Tạo Account
        Account account = Account.builder()
                .username(newUsername) // 👈 10001
                .staffId(newUsername)  // 👈 10001 (liên kết)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD)) // 👈 Mã hóa "123"
                .enabled(true)
                .roles(Set.of(role))
                .build();

        accountRepository.save(account);
        log.info("Internal account created successfully: {}", newUsername);

        // 4. Trả về ID/Username
        return ApiResponse.<InternalAccountResponse>builder()
                .result(InternalAccountResponse.builder()
                        .staffId(newUsername)
                        .username(newUsername)
                        .build())
                .build();
    }
}