package com.example.hrservice.service;


import com.example.hrservice.DTO.request.ApplicantHireRequest;
import com.example.hrservice.DTO.request.ApplicantRequest;
import com.example.hrservice.DTO.request.InternalAccountRequest;
import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.DTO.response.ApplicantResponse;
import com.example.hrservice.DTO.response.InternalAccountResponse;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.entity.Applicant;
import com.example.hrservice.entity.PositionSalaryConfig;
import com.example.hrservice.enums.ApplicantStatus;
import com.example.hrservice.enums.EmployeeType;
import com.example.hrservice.enums.Position;
import com.example.hrservice.exception.AppException;
import com.example.hrservice.exception.ErrorCode;
import com.example.hrservice.repository.ApplicantRepository;
import com.example.hrservice.repository.PositionSalaryConfigRepository;
import jakarta.annotation.PostConstruct; // 👈 Import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final PositionSalaryConfigRepository salaryConfigRepository;
    private final ModelMapper modelMapper;
    private final StaffService staffService;
    private final RestTemplate restTemplate;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    // Thư mục lưu trữ CV (nên cấu hình volume trong Docker)
    private final Path rootLocation = Paths.get("uploads");

    /**
     * Khởi tạo thư mục lưu trữ khi Service được tạo
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage location", e);
            throw new AppException(ErrorCode.FILE_STORAGE_FAILED);
        }
    }

    /**
     * API Public: Nộp đơn ứng tuyển (kèm file CV)
     */
    @Transactional
    public ApplicantResponse submitApplication(ApplicantRequest request, MultipartFile cvFile) {
        log.info("Nhận đơn ứng tuyển mới cho vị trí: {}", request.getPositionApplied());

        if (cvFile == null || cvFile.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_REQUIRED);
        }

        // Lưu file CV
        String extension = com.google.common.io.Files.getFileExtension(cvFile.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
        String cvPath;
        try {
            Path destinationFile = this.rootLocation.resolve(uniqueFileName);
            Files.copy(cvFile.getInputStream(), destinationFile);
            cvPath = destinationFile.toString();
        } catch (IOException e) {
            log.error("Lỗi khi lưu file CV", e);
            throw new AppException(ErrorCode.FILE_STORAGE_FAILED);
        }

        // Lưu Applicant
        Applicant applicant = modelMapper.map(request, Applicant.class);
        applicant.setStatus(ApplicantStatus.PENDING);
        applicant.setCvUrl(cvPath);

        Applicant savedApplicant = applicantRepository.save(applicant);
        return modelMapper.map(savedApplicant, ApplicantResponse.class);
    }

    /**
     * API Internal: Lấy danh sách ứng viên theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<ApplicantResponse> getApplicantsByStatus(ApplicantStatus status) {
        log.info("Đang tìm ứng viên với trạng thái: {}", status);
        List<Applicant> applicants = applicantRepository.findByStatus(status);

        return applicants.stream()
                .map(applicant -> modelMapper.map(applicant, ApplicantResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * API Internal: Tuyển dụng (Hire) ứng viên
     * -> Tự động gọi Auth Service để tạo tài khoản
     * -> Tự động tạo Staff Record với thông tin đã merge
     */
    @Transactional
    public StaffResponse hireApplicant(String applicantId, ApplicantHireRequest hireRequest) {

        // 1. Tìm ứng viên
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICANT_NOT_FOUND));

        if (applicant.getStatus() == ApplicantStatus.HIRED) {
            throw new AppException(ErrorCode.APPLICANT_ALREADY_HIRED);
        }

        // 2. Xử lý Lương & Loại hình nhân viên (Fulltime/Parttime)
        // Nếu Manager không chọn loại hình, mặc định là FULL_TIME
        EmployeeType type = (hireRequest.getEmployeeType() != null) ? hireRequest.getEmployeeType() : EmployeeType.FULL_TIME;
        BigDecimal finalSalary = hireRequest.getSalary();

        Position position = applicant.getPositionApplied();
        if (finalSalary == null) {
            // Nếu Manager không nhập lương, lấy từ cấu hình lương chuẩn trong DB
            PositionSalaryConfig config = salaryConfigRepository.findByPositionAndEmployeeType(position, type)
                    .orElseThrow(() -> new RuntimeException("Chưa cấu hình lương cho vị trí " + position + " (" + type + ")"));
            finalSalary = config.getBaseSalary();
            log.info("Sử dụng lương mặc định cho {}: {}", position, finalSalary);
        }

        // 3. Tự động suy ra Role và JobTitle từ Enum Position
        String securityRole = position.getDefaultSecurityRole(); // VD: "ROLE_STAFF"
        String jobTitle = position.getJobTitle();         // VD: "Pha chế viên"

        // 4. Gọi Auth Service (Đồng bộ) để tạo tài khoản và lấy ID mới
        log.info("Gọi auth-service để tạo tài khoản với vai trò: {}", securityRole);

        InternalAccountRequest authRequest = new InternalAccountRequest();
        authRequest.setRole(securityRole); // Gửi Role đã suy ra

        // Cấu hình kiểu trả về cho RestTemplate (để tránh lỗi LinkedHashMap)
        ParameterizedTypeReference<ApiResponse<InternalAccountResponse>> responseType =
                new ParameterizedTypeReference<>() {};

        // Gọi API nội bộ của auth-service
        ResponseEntity<ApiResponse<InternalAccountResponse>> responseEntity = restTemplate.exchange(
                authServiceUrl + "/api/auth/internal/create-account",
                HttpMethod.POST,
                new HttpEntity<>(authRequest),
                responseType
        );

        ApiResponse<InternalAccountResponse> authResponse = responseEntity.getBody();
        if (authResponse == null || authResponse.getResult() == null) {
            throw new RuntimeException("Lỗi khi gọi Auth Service: Phản hồi rỗng");
        }

        // Lấy ID mới (ví dụ: "10001")
        String newStaffId = authResponse.getResult().getStaffId();
        log.info("Auth-service đã tạo account thành công, trả về staffId: {}", newStaffId);

        // 5. Tách tên (Firstname, Lastname) từ Fullname của ứng viên
        String firstname = applicant.getFullName();
        String lastname = "";
        if (applicant.getFullName() != null && applicant.getFullName().contains(" ")) {
            int lastSpace = applicant.getFullName().lastIndexOf(" ");
            firstname = applicant.getFullName().substring(0, lastSpace);
            lastname = applicant.getFullName().substring(lastSpace + 1);
        }

        // 6. Xây dựng DTO để tạo Staff (Merge dữ liệu cũ và mới)
        StaffCreationRequest staffRequest = new StaffCreationRequest();
        // Từ hồ sơ ứng viên
        staffRequest.setFirstname(firstname);
        staffRequest.setLastname(lastname);
        // Từ yêu cầu tuyển dụng của Manager
        staffRequest.setShopId(hireRequest.getShopId());
        staffRequest.setSalary(finalSalary);
        staffRequest.setHireDate(hireRequest.getHireDate());
        staffRequest.setDob(hireRequest.getDob());
        staffRequest.setGender(hireRequest.getGender());
        staffRequest.setEmployeeType(type);
        // Từ logic suy luận
        staffRequest.setRole(securityRole);
        staffRequest.setJobTitle(jobTitle);

        // 7. Gọi StaffService để lưu nhân viên (với ID đã được cấp)
        StaffResponse staffResponse = staffService.createStaff(staffRequest, newStaffId);

        // 8. Cập nhật trạng thái ứng viên thành HIRED
        applicant.setStatus(ApplicantStatus.HIRED);
        applicantRepository.save(applicant);

        return staffResponse;
    }
}