package com.example.hrservice.service;

import com.example.hrservice.DTO.response.PayslipResponse;
import com.example.hrservice.entity.*;
import com.example.hrservice.enums.AllowanceBasis;
import com.example.hrservice.enums.EmployeeType;
import com.example.hrservice.exception.AppException; // 👈 Import
import com.example.hrservice.exception.ErrorCode;  // 👈 Import
import com.example.hrservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final StaffRepository staffRepository;
    private final StaffAllowanceRepository allowanceRepository;
    private final BonusRepository bonusRepository;
    private final PayslipRepository payslipRepository;
    private final SalaryAdvanceRepository advanceRepository;
    private final ModelMapper modelMapper;

    private static final BigDecimal UNION_FEE = BigDecimal.valueOf(50000);
    private static final BigDecimal BHYT_PERCENTAGE = BigDecimal.valueOf(0.08);

    @Transactional
    public PayslipResponse generatePayslip(String staffId, Double totalHoursInput) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        LocalDate period = LocalDate.now().withDayOfMonth(1);

        // 👇 SỬA LẠI ĐOẠN NÀY CHO CHUẨN
        if (payslipRepository.findByStaffIdAndSalaryPeriod(staffId, period).isPresent()) {
            throw new AppException(ErrorCode.PAYSLIP_EXISTED);
        }

        // --- 1. XỬ LÝ GIỜ LÀM ---
        Double actualHours = (totalHoursInput != null) ? totalHoursInput : 0.0;

        // --- 2. TÍNH THU NHẬP (INCOME) ---
        BigDecimal baseIncome;

        if (staff.getEmployeeType() == EmployeeType.PART_TIME) {
            // Part-time: Lương giờ * Số giờ
            baseIncome = staff.getSalary().multiply(BigDecimal.valueOf(actualHours));
        } else {
            // Full-time: Lương cứng
            baseIncome = staff.getSalary();
        }

        // Cộng Phụ cấp
        List<StaffAllowance> allowances = allowanceRepository.findByStaffId(staffId);
        BigDecimal totalAllowances = BigDecimal.ZERO;

        for (StaffAllowance a : allowances) {
            if (a.getAllowanceBasis() == AllowanceBasis.MONTHLY_FIXED) {
                totalAllowances = totalAllowances.add(a.getAmount());
            } else {
                // Phụ cấp theo giờ
                if (actualHours > 0) {
                    totalAllowances = totalAllowances.add(a.getAmount().multiply(BigDecimal.valueOf(actualHours)));
                }
            }
        }

        // Cộng Thưởng
        List<Bonus> bonuses = bonusRepository.findByStaffIdOrderByDateDesc(staffId);
        BigDecimal totalBonuses = bonuses.stream()
                .map(Bonus::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossSalary = baseIncome.add(totalAllowances).add(totalBonuses);

        // --- 3. TÍNH KHẤU TRỪ (DEDUCTIONS) ---
        BigDecimal healthInsurance = BigDecimal.ZERO;
        BigDecimal unionFeeDeduction = BigDecimal.ZERO;

        if (staff.getEmployeeType() == EmployeeType.FULL_TIME) {
            healthInsurance = baseIncome.multiply(BHYT_PERCENTAGE);
            unionFeeDeduction = UNION_FEE;
        }

        // Tính tổng tiền tạm ứng đã duyệt
        LocalDate startOfMonth = period;
        LocalDate endOfMonth = period.plusMonths(1).minusDays(1);
        List<SalaryAdvance> advances = advanceRepository.findApprovedAdvancesInMonth(staffId, startOfMonth, endOfMonth);

        BigDecimal totalAdvances = advances.stream()
                .map(SalaryAdvance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeductions = healthInsurance.add(unionFeeDeduction).add(totalAdvances);

        // --- 4. KẾT QUẢ ---
        BigDecimal netSalary = grossSalary.subtract(totalDeductions);

        // 5. Lưu Payslip
        Payslip payslip = Payslip.builder()
                .staffId(staffId)
                .salaryPeriod(period)
                .baseSalary(baseIncome)
                .totalAllowances(totalAllowances)
                .totalBonuses(totalBonuses)
                .totalHoursWorked(actualHours)
                .grossSalary(grossSalary)
                .healthInsurance(healthInsurance)
                .unionFee(unionFeeDeduction)
                .totalAdvances(totalAdvances)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .isPaid(false)
                .build();

        Payslip savedPayslip = payslipRepository.save(payslip);

        PayslipResponse response = modelMapper.map(savedPayslip, PayslipResponse.class);
        response.setStaffName(staff.getFirstname() + " " + staff.getLastname());
        response.setJobTitle(staff.getJobTitle());

        return response;
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> getMyPayslips(String staffId) {
        List<Payslip> payslips = payslipRepository.findByStaffIdOrderBySalaryPeriodDesc(staffId);
        return payslips.stream()
                .map(p -> modelMapper.map(p, PayslipResponse.class))
                .toList();
    }
}