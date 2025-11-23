package com.example.hrservice.service;

import com.example.hrservice.DTO.request.AdvanceRequest;
import com.example.hrservice.entity.SalaryAdvance;
import com.example.hrservice.entity.Staff;
import com.example.hrservice.enums.LeaveStatus;
import com.example.hrservice.exception.AppException;
import com.example.hrservice.exception.ErrorCode;
import com.example.hrservice.repository.SalaryAdvanceRepository;
import com.example.hrservice.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvanceService {

    private final SalaryAdvanceRepository advanceRepository;
    private final StaffRepository staffRepository;
    private final StaffService staffService;

    // Giới hạn ứng tối đa (ví dụ: 50% lương)
    private static final BigDecimal MAX_ADVANCE_PERCENTAGE = BigDecimal.valueOf(0.5);

    @Transactional
    public SalaryAdvance requestAdvance(AdvanceRequest request) {
        String staffId = staffService.getStaffIdFromToken();
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // Kiểm tra logic: Không được ứng quá 50% lương cơ bản
        BigDecimal maxAdvance = staff.getSalary().multiply(MAX_ADVANCE_PERCENTAGE);
        if (request.getAmount().compareTo(maxAdvance) > 0) {
            throw new RuntimeException("Cannot advance more than 50% of base salary (" + maxAdvance + ")");
        }

        SalaryAdvance advance = SalaryAdvance.builder()
                .staffId(staffId)
                .amount(request.getAmount())
                .reason(request.getReason())
                .dateNeeded(request.getDateNeeded())
                .status(LeaveStatus.PENDING)
                .build();

        return advanceRepository.save(advance);
    }

    @Transactional
    public SalaryAdvance approveAdvance(Long advanceId, boolean approved) {
        SalaryAdvance advance = advanceRepository.findById(advanceId)
                .orElseThrow(() -> new RuntimeException("Advance request not found"));

        advance.setStatus(approved ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        return advanceRepository.save(advance);
    }

    public List<SalaryAdvance> getMyAdvances() {
        return advanceRepository.findByStaffId(staffService.getStaffIdFromToken());
    }
}