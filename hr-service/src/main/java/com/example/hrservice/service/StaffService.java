package com.example.hrservice.service;

import com.example.hrservice.DTO.event.AccountCreationEvent;
import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.config.RabbitMQConfig;
import com.example.hrservice.entity.Staff;
import com.example.hrservice.enums.Status;
import com.example.hrservice.repository.StaffRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;
    private final RabbitTemplate rabbitTemplate; // 👈 Tiêm RabbitTemplate

    @Transactional
    public StaffResponse createStaff(StaffCreationRequest request) {

        // 1. Tạo và lưu Staff (Hồ sơ nhân viên)
        Staff staff = Staff.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .dob(request.getDob())
                .gender(request.getGender())
                .shopId(request.getShopId())
                .hireDate(request.getHireDate())
                .salary(request.getSalary())
                .status(Status.PROBATION) // Mặc định là Thử việc
                .build();

        Staff savedStaff = staffRepository.save(staff);
        log.info("Đã tạo hồ sơ Staff với ID: {}", savedStaff.getId());

        // 2. Chuẩn bị tin nhắn để gửi đi
        AccountCreationEvent event = AccountCreationEvent.builder()
                .staffId(savedStaff.getId()) // 👈 Liên kết quan trọng
                .username(request.getUsername())
                .password(request.getPassword()) // auth-service sẽ mã hóa
                .role(request.getRole())
                .build();

        // 3. Gửi tin nhắn tới RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
        log.info("Đã gửi AccountCreationEvent cho Staff ID: {}", savedStaff.getId());

        // 4. Trả về thông tin Staff đã tạo
        return StaffResponse.builder()
                .staffId(savedStaff.getId())
                .firstname(savedStaff.getFirstname())
                .lastname(savedStaff.getLastname())
                .shopId(savedStaff.getShopId())
                .status(savedStaff.getStatus())
                .salary(savedStaff.getSalary())
                .hireDate(savedStaff.getHireDate())
                .build();
    }
}