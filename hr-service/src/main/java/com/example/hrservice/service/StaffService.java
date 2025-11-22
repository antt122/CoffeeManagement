package com.example.hrservice.service;

import com.example.hrservice.config.RabbitMQConfig;
import com.example.hrservice.DTO.event.AccountUpdateEvent;
import com.example.hrservice.DTO.request.ProfileUpdateRequest;
import com.example.hrservice.DTO.request.SalaryUpdateRequest;
import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.request.StaffPromotionRequest;
import com.example.hrservice.DTO.response.SalaryHistoryResponse;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.entity.SalaryHistory;
import com.example.hrservice.entity.Staff;
import com.example.hrservice.enums.Status;
import com.example.hrservice.exception.AppException;
import com.example.hrservice.exception.ErrorCode;
import com.example.hrservice.repository.SalaryHistoryRepository;
import com.example.hrservice.repository.StaffRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
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
    private final SalaryHistoryRepository salaryHistoryRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ModelMapper modelMapper;

    /**
     * Cấu hình ModelMapper để tránh lỗi xung đột ID
     * (shopId vs id)
     */
    @PostConstruct
    public void setupMapper() {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Tạo TypeMap riêng cho StaffCreationRequest -> Staff nếu chưa có
        if (modelMapper.getTypeMap(StaffCreationRequest.class, Staff.class) == null) {
            modelMapper.createTypeMap(StaffCreationRequest.class, Staff.class)
                    .addMappings(mapper -> mapper.skip(Staff::setId)); // Bỏ qua ID, gán thủ công
        }
    }

    @Transactional
    public StaffResponse createStaff(StaffCreationRequest request, String generatedStaffId) {
        // 1. Map DTO -> Entity (ModelMapper đã được cấu hình skip ID)
        Staff staff = modelMapper.map(request, Staff.class);

        // 2. Gán ID thủ công (từ Auth Service)
        staff.setId(generatedStaffId);
        staff.setStatus(Status.PROBATION);

        // 3. Lưu vào DB
        Staff savedStaff = staffRepository.save(staff);
        log.info("Đã tạo hồ sơ Staff với ID: {}", savedStaff.getId());

        return modelMapper.map(savedStaff, StaffResponse.class);
    }

    @Transactional
    public StaffResponse promoteStaff(String staffId, StaffPromotionRequest request) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        staff.setStatus(request.getNewStatus());
        Staff savedStaff = staffRepository.save(staff);
        log.info("Cập nhật trạng thái Staff ID: {} thành {}", staffId, request.getNewStatus());

        // Gửi event cập nhật Role cho Auth Service
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
        String staffId = getStaffIdFromToken();

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        return modelMapper.map(staff, StaffResponse.class);
    }

    @Transactional
    public StaffResponse updateMyInfo(ProfileUpdateRequest request) {
        String staffId = getStaffIdFromToken();
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // Chỉ map các trường không null
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(request, staff);
        modelMapper.getConfiguration().setSkipNullEnabled(false); // Reset lại

        Staff updatedStaff = staffRepository.save(staff);
        log.info("Nhân viên {} đã cập nhật hồ sơ.", staffId);

        return modelMapper.map(updatedStaff, StaffResponse.class);
    }

    @Transactional
    public StaffResponse updateSalary(String staffId, SalaryUpdateRequest request) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // Lưu lịch sử lương
        SalaryHistory history = SalaryHistory.builder()
                .staffId(staff.getId())
                .oldSalary(staff.getSalary())
                .newSalary(request.getNewSalary())
                .reason(request.getReason())
                .updatedBy(getStaffIdFromToken())
                .build();
        salaryHistoryRepository.save(history);

        // Cập nhật lương mới
        staff.setSalary(request.getNewSalary());
        return modelMapper.map(staffRepository.save(staff), StaffResponse.class);
    }

    @Transactional(readOnly = true)
    public List<SalaryHistoryResponse> getSalaryHistory(String staffId) {
        if (!staffRepository.existsById(staffId)) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        }
        return salaryHistoryRepository.findByStaffIdOrderByChangedAtDesc(staffId).stream()
                .map(h -> modelMapper.map(h, SalaryHistoryResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffResponse terminateStaff(String staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        staff.setStatus(Status.TERMINATED);
        Staff savedStaff = staffRepository.save(staff);
        log.info("Đã cập nhật trạng thái Staff ID: {} thành TERMINATED", staffId);

        // Gửi event khóa tài khoản
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

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByStatus(Status status) {
        log.info("Đang tìm nhân viên với trạng thái: {}", status);
        List<Staff> staffList = staffRepository.findByStatus(status);

        return staffList.stream()
                .map(staff -> modelMapper.map(staff, StaffResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * Helper: Lấy ID từ Token
     */
    public String getStaffIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String staffId = jwt.getClaimAsString("staffId");
            if (staffId != null) return staffId;
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}