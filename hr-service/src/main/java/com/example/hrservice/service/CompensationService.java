package com.example.hrservice.service;


import com.example.hrservice.DTO.request.AllowanceRequest;
import com.example.hrservice.DTO.request.BonusRequest;
import com.example.hrservice.entity.Bonus;
import com.example.hrservice.entity.StaffAllowance;
import com.example.hrservice.exception.AppException;
import com.example.hrservice.exception.ErrorCode;
import com.example.hrservice.repository.BonusRepository;
import com.example.hrservice.repository.StaffAllowanceRepository;
import com.example.hrservice.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompensationService {

    private final StaffAllowanceRepository allowanceRepository;
    private final BonusRepository bonusRepository;
    private final StaffRepository staffRepository;
    private final ModelMapper modelMapper; // (Optional nếu bạn muốn map DTO trả về)

    // --- PHẦN PHỤ CẤP (ALLOWANCE) ---

    @Transactional
    public StaffAllowance addAllowance(String staffId, AllowanceRequest request) {
        // Kiểm tra nhân viên tồn tại
        if (!staffRepository.existsById(staffId)) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        }

        StaffAllowance allowance = StaffAllowance.builder()
                .staffId(staffId)
                .allowanceType(request.getAllowanceType())
                .allowanceBasis(request.getAllowanceBasis())
                .amount(request.getAmount())
                .build();

        return allowanceRepository.save(allowance);
    }

    @Transactional(readOnly = true)
    public List<StaffAllowance> getAllowances(String staffId) {
        return allowanceRepository.findByStaffId(staffId);
    }

    @Transactional
    public void removeAllowance(Long allowanceId) {
        allowanceRepository.deleteById(allowanceId);
    }

    // --- PHẦN THƯỞNG (BONUS) ---

    @Transactional
    public Bonus addBonus(String staffId, BonusRequest request) {
        if (!staffRepository.existsById(staffId)) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        }

        Bonus bonus = Bonus.builder()
                .staffId(staffId)
                .amount(request.getAmount())
                .reason(request.getReason())
                .build(); // date tự động tạo

        log.info("Thưởng nóng cho Staff {}: {}", staffId, request.getAmount());
        return bonusRepository.save(bonus);
    }

    @Transactional(readOnly = true)
    public List<Bonus> getBonuses(String staffId) {
        return bonusRepository.findByStaffIdOrderByDateDesc(staffId);
    }
}