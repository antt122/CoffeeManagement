package com.example.hrservice.service;


import com.example.hrservice.DTO.event.AccountUpdateEvent;
import com.example.hrservice.DTO.request.ProfileUpdateRequest;
import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.request.StaffPromotionRequest;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.config.RabbitMQConfig;
import com.example.hrservice.entity.Staff;
import com.example.hrservice.enums.Status;
import com.example.hrservice.exception.AppException;
import com.example.hrservice.exception.ErrorCode;
import com.example.hrservice.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {
    private final StaffRepository staffRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ModelMapper modelMapper;

    @Transactional
    public StaffResponse createStaff(StaffCreationRequest request, String generatedStaffId) {
        Staff staff = modelMapper.map(request, Staff.class);
        staff.setId(generatedStaffId);
        staff.setStatus(Status.PROBATION);

        Staff savedStaff = staffRepository.save(staff);
        log.info("Đã tạo hồ sơ Staff với ID: {}", savedStaff.getId());

        return modelMapper.map(savedStaff, StaffResponse.class);
    }

    @Transactional
    public StaffResponse promoteStaff(String staffId, StaffPromotionRequest request) {
        Staff staff = staffRepository.findById(staffId)
                // 👇 SỬA LỖI
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        staff.setStatus(request.getNewStatus());
        Staff savedStaff = staffRepository.save(staff);
        log.info("Cập nhật trạng thái Staff ID: {} thành {}", staffId, request.getNewStatus());

        AccountUpdateEvent event = AccountUpdateEvent.builder()
                .staffId(staffId)
                .newRole(request.getNewRole())
                .enabled(request.getNewStatus() == Status.ACTIVE)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROLE_UPDATE_ROUTING_KEY,
                event
        );
        log.info("Đã gửi AccountUpdateEvent cho Staff ID: {}", staffId);

        return modelMapper.map(savedStaff, StaffResponse.class);
    }

    @Transactional(readOnly = true)
    public StaffResponse getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String staffId;
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            staffId = jwt.getClaimAsString("staffId");
        } else {
            // 👇 SỬA LỖI
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (staffId == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        Staff staff = staffRepository.findById(staffId)
                // 👇 SỬA LỖI
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        return modelMapper.map(staff, StaffResponse.class);
    }

    @Transactional
    public StaffResponse terminateStaff(String staffId) {
        Staff staff = staffRepository.findById(staffId)
                // 👇 SỬA LỖI
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        staff.setStatus(Status.TERMINATED);
        Staff savedStaff = staffRepository.save(staff);
        log.info("Đã cập nhật trạng thái Staff ID: {} thành TERMINATED", staffId);

        AccountUpdateEvent event = AccountUpdateEvent.builder()
                .staffId(staffId)
                .enabled(false)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ACCOUNT_DISABLE_ROUTING_KEY,
                event
        );
        log.info("Đã gửi AccountDisableEvent cho Staff ID: {}", staffId);

        return modelMapper.map(savedStaff, StaffResponse.class);
    }

    @Transactional
    public StaffResponse updateMyInfo(ProfileUpdateRequest request) {
        // 1. Lấy staffId từ token (Tái sử dụng hàm helper)
        String staffId = getStaffIdFromToken();

        // 2. Tìm hồ sơ Staff
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // 3. (QUAN TRỌNG) Cập nhật các trường được phép
        // Dùng ModelMapper để map các trường (firstname, lastname, dob...)
        // Bỏ qua các trường null (nếu người dùng không gửi lên)
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(request, staff);

        // 4. Lưu lại
        Staff updatedStaff = staffRepository.save(staff);
        log.info("Nhân viên {} đã cập nhật hồ sơ.", staffId);

        // 5. Trả về hồ sơ đã cập nhật
        return modelMapper.map(updatedStaff, StaffResponse.class);
    }


    /**
     * (HÀM HELPER - TÁCH RA TỪ getMyInfo)
     * Đọc staffId từ claim trong JWT token
     */
    private String getStaffIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String staffId;
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            staffId = jwt.getClaimAsString("staffId");
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (staffId == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return staffId;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByStatus(Status status) {
        log.info("Đang tìm nhân viên với trạng thái: {}", status);

        List<Staff> staffList = staffRepository.findByStatus(status);

        // Dùng ModelMapper để chuyển đổi List
        return staffList.stream()
                .map(staff -> modelMapper.map(staff, StaffResponse.class))
                .collect(Collectors.toList());
    }
}