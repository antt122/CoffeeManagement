package com.example.hrservice.service;

import com.example.hrservice.DTO.request.LeaveApprovalRequest;
import com.example.hrservice.DTO.request.LeaveRequestInput;
import com.example.hrservice.DTO.response.LeaveRequestResponse;
import com.example.hrservice.entity.LeaveRequest;
import com.example.hrservice.entity.Staff;
import com.example.hrservice.enums.LeaveStatus;
import com.example.hrservice.enums.LeaveType;
import com.example.hrservice.exception.AppException;
import com.example.hrservice.exception.ErrorCode;
import com.example.hrservice.repository.LeaveRequestRepository;
import com.example.hrservice.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffRepository staffRepository;
    private final StaffService staffService; // Để lấy staffId từ token
    private final ModelMapper modelMapper;

    @Transactional
    public LeaveRequestResponse applyForLeave(LeaveRequestInput input) {
        // 1. Lấy nhân viên
        String staffId = staffService.getStaffIdFromToken();
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // 2. Tính số ngày
        double numberOfDays = ChronoUnit.DAYS.between(input.getStartDate(), input.getEndDate()) + 1;

        // 3. (SỬA LỖI Ở ĐÂY) Kiểm tra số dư
        if (input.getLeaveType() == LeaveType.ANNUAL_LEAVE) {

            // Lấy số dư và kiểm tra null an toàn
            Double currentBalance = staff.getAnnualLeaveBalance();
            double balance = (currentBalance == null) ? 0.0 : currentBalance.doubleValue();

            if (balance < numberOfDays) {
                throw new AppException(ErrorCode.INSUFFICIENT_LEAVE_BALANCE);
            }
        }

        // 4. Lưu đơn
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .staff(staff)
                .leaveType(input.getLeaveType())
                .startDate(input.getStartDate())
                .endDate(input.getEndDate())
                .numberOfDays(numberOfDays)
                .reason(input.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        return modelMapper.map(savedRequest, LeaveRequestResponse.class);
    }

    @Transactional
    public LeaveRequestResponse reviewLeaveRequest(String leaveRequestId, LeaveApprovalRequest input) {
        // 1. Tìm đơn
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_REQUEST_NOT_FOUND)); // (Thêm ErrorCode)

        // 2. Chỉ trừ phép nếu:
        //    - Đơn được DUYỆT (APPROVED)
        //    - Và loại nghỉ là ANNUAL_LEAVE
        if (input.getNewStatus() == LeaveStatus.APPROVED &&
                leaveRequest.getLeaveType() == LeaveType.ANNUAL_LEAVE) {

            Staff staff = leaveRequest.getStaff();
            double newBalance = staff.getAnnualLeaveBalance() - leaveRequest.getNumberOfDays();

            if (newBalance < 0) {
                // (Manager duyệt nhưng nhân viên không đủ phép)
                throw new AppException(ErrorCode.INSUFFICIENT_LEAVE_BALANCE);
            }

            staff.setAnnualLeaveBalance(newBalance);
            staffRepository.save(staff);
            log.info("Đã trừ {} ngày phép của Staff ID: {}", leaveRequest.getNumberOfDays(), staff.getId());
        }

        // 3. Cập nhật trạng thái đơn
        leaveRequest.setStatus(input.getNewStatus());
        leaveRequest.setManagerNotes(input.getManagerNotes());

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        return modelMapper.map(savedRequest, LeaveRequestResponse.class);
    }
    @Transactional(readOnly = true) // Cần Transactional vì nó đọc Entity Staff
    public List<LeaveRequestResponse> getMyLeaveRequests() {
        // 1. Lấy staffId từ token
        String staffId = staffService.getStaffIdFromToken();

        // 2. Lấy danh sách đơn từ DB
        List<LeaveRequest> requests = leaveRequestRepository.findByStaff_IdOrderByRequestedAtDesc(staffId);

        // 3. Map sang DTO
        return requests.stream()
                .map(request -> modelMapper.map(request, LeaveRequestResponse.class))
                .collect(Collectors.toList());
    }
}
