package com.example.coffeemanager.service;


import com.example.coffeemanager.DTO.event.AccountCreationEvent;
import com.example.coffeemanager.DTO.event.AccountUpdateEvent;
import com.example.coffeemanager.config.RabbitMQConfig;
import com.example.coffeemanager.entity.Account;
import com.example.coffeemanager.entity.Role;
import com.example.coffeemanager.repository.AccountRepository;
import com.example.coffeemanager.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventListener {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lắng nghe hàng đợi "auth-account-creation-queue".
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAccountCreation(AccountCreationEvent event) {
        log.info("Received account creation event for staffId: {}", event.getStaffId());

        try {
            // 1. Kiểm tra xem username đã tồn tại chưa
            if (accountRepository.findByUsername(event.getUsername()).isPresent()) {
                log.warn("Username {} already exists. Skipping account creation.", event.getUsername());
                // (Trong thực tế, bạn có thể gửi 1 tin nhắn lỗi trả lại cho hr-service)
                return;
            }

            // 2. Tìm vai trò (Role)
            Role role = roleRepository.findByName(event.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + event.getRole()));

            // 3. Tạo tài khoản
            Account account = Account.builder()
                    .username(event.getUsername())
                    .password(passwordEncoder.encode(event.getPassword())) // Mã hóa mật khẩu
                    .staffId(event.getStaffId()) // Liên kết với Staff
                    .enabled(true)
                    .roles(Set.of(role))
                    .build();

            accountRepository.save(account);
            log.info("Successfully created account for username: {}", event.getUsername());

        } catch (Exception e) {
            log.error("Failed to create account for staffId: {}. Error: {}",
                    event.getStaffId(), e.getMessage());
            // (Xử lý lỗi, ví dụ: đưa tin nhắn vào Dead Letter Queue)
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME_UPDATE)
    @Transactional
    public void handleAccountUpdate(AccountUpdateEvent event) {
        log.info("Received account update event for staffId: {}", event.getStaffId());

        try {
            // 1. Tìm tài khoản
            Account account = accountRepository.findByStaffId(event.getStaffId())
                    .orElseThrow(() -> new RuntimeException("Account not found for staffId: " + event.getStaffId()));

            // 2. Tìm vai trò mới
            Role newRole = roleRepository.findByName(event.getNewRole())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + event.getNewRole()));

            // 3. Cập nhật
            account.setRoles(Set.of(newRole));
            if(event.getEnabled() != null) {
                account.setEnabled(event.getEnabled());
            }

            accountRepository.save(account);
            log.info("Successfully updated account for staffId: {}", event.getStaffId());

        } catch (Exception e) {
            log.error("Failed to update account for staffId: {}. Error: {}",
                    event.getStaffId(), e.getMessage());
        }
    }
}
