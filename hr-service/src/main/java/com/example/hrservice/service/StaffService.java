package com.example.hrservice.service;

import com.example.hrservice.DTO.event.AccountCreationEvent;
import com.example.hrservice.DTO.event.AccountUpdateEvent;
import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.request.StaffPromotionRequest;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.config.RabbitMQConfig;
import com.example.hrservice.entity.Staff;
import com.example.hrservice.enums.Status;
import com.example.hrservice.repository.StaffRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {
        private final StaffRepository staffRepository;
        private final RabbitTemplate rabbitTemplate;
        private final ModelMapper modelMapper; // 👈 Tiêm ModelMapper



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
                    .status(Status.PROBATION)
                    .build();

            Staff savedStaff = staffRepository.save(staff);
            log.info("Đã tạo hồ sơ Staff với ID: {}", savedStaff.getId());

            // 2. CHUẨN BỊ EVENT (ĐOẠN NÀY BỊ THIẾU LÚC TRƯỚC)
            AccountCreationEvent event = AccountCreationEvent.builder()
                    .staffId(savedStaff.getId())
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .build();

            // 3. Gửi tin nhắn RabbitMQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    event // 👈 Bây giờ 'event' đã được định nghĩa
            );
            log.info("Đã gửi AccountCreationEvent cho Staff ID: {}", savedStaff.getId());

            // 4. Trả về thông tin (Dùng ModelMapper)
            return modelMapper.map(savedStaff, StaffResponse.class);
        }

        @Transactional
        public StaffResponse promoteStaff(String staffId, StaffPromotionRequest request) {
            // 1. Tìm nhân viên
            Staff staff = staffRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found: " + staffId));

            // 2. Cập nhật hồ sơ
            staff.setStatus(request.getNewStatus());
            Staff savedStaff = staffRepository.save(staff);
            log.info("Cập nhật trạng thái Staff ID: {} thành {}", staffId, request.getNewStatus());

            // 3. CHUẨN BỊ EVENT (ĐOẠN NÀY BỊ THIẾU LÚC TRƯỚC)
            AccountUpdateEvent event = AccountUpdateEvent.builder()
                    .staffId(staffId)
                    .newRole(request.getNewRole())
                    .enabled(request.getNewStatus() == Status.ACTIVE)
                    .build();

            // 4. Gửi tin nhắn RabbitMQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROLE_UPDATE_ROUTING_KEY,
                    event // 👈 Bây giờ 'event' đã được định nghĩa
            );
            log.info("Đã gửi AccountUpdateEvent cho Staff ID: {}", staffId);

            // 5. Trả về thông tin (Dùng ModelMapper)
            return modelMapper.map(savedStaff, StaffResponse.class);
        }
    }