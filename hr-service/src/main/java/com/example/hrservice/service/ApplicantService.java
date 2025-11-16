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
import com.example.hrservice.enums.ApplicantStatus;
import com.example.hrservice.enums.Position;
import com.example.hrservice.exception.AppException;
import com.example.hrservice.exception.ErrorCode;
import com.example.hrservice.repository.ApplicantRepository;
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
    private final ModelMapper modelMapper;
    private final StaffService staffService;
    private final RestTemplate restTemplate;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    private final Path rootLocation = Paths.get("uploads");

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage location", e);
            throw new AppException(ErrorCode.FILE_STORAGE_FAILED);
        }
    }

    @Transactional
    public ApplicantResponse submitApplication(ApplicantRequest request, MultipartFile cvFile) {
        log.info("Nhận đơn ứng tuyển mới cho vị trí: {}", request.getPositionApplied());

        if (cvFile == null || cvFile.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_REQUIRED);
        }

        String extension = com.google.common.io.Files.getFileExtension(cvFile.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

        String cvPath;
        try {
            Path destinationFile = this.rootLocation.resolve(uniqueFileName);
            Files.copy(cvFile.getInputStream(), destinationFile);
            cvPath = destinationFile.toString();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_STORAGE_FAILED);
        }

        Applicant applicant = modelMapper.map(request, Applicant.class);
        applicant.setStatus(ApplicantStatus.PENDING);
        applicant.setCvUrl(cvPath);

        Applicant savedApplicant = applicantRepository.save(applicant);

        return modelMapper.map(savedApplicant, ApplicantResponse.class);
    }

    @Transactional(readOnly = true)
    public List<ApplicantResponse> getApplicantsByStatus(ApplicantStatus status) {
        log.info("Đang tìm ứng viên với trạng thái: {}", status);
        List<Applicant> applicants = applicantRepository.findByStatus(status);

        return applicants.stream()
                .map(applicant -> modelMapper.map(applicant, ApplicantResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * HÀM HIRE (ĐÃ SỬA LẠI LOGIC)
     * (Tự động suy ra Role và JobTitle từ Position của Ứng viên)
     */
    @Transactional
    public StaffResponse hireApplicant(String applicantId, ApplicantHireRequest hireRequest) {

        // 1. Tìm ứng viên
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICANT_NOT_FOUND));

        if (applicant.getStatus() == ApplicantStatus.HIRED) {
            throw new AppException(ErrorCode.APPLICANT_ALREADY_HIRED);
        }

        // 2. (MỚI) Tự động suy ra (infer) Role và JobTitle từ Enum
        Position position = applicant.getPositionApplied(); // Ví dụ: Position.BARISTA
        if (position == null) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR); // Hoặc lỗi "Vị trí không hợp lệ"
        }

        String securityRole = position.getDefaultSecurityRole(); // -> "ROLE_STAFF"
        String jobTitle = position.getJobTitle();         // -> "Pha chế viên"

        // 3. (SỬA) Gọi auth-service với Role đã được suy ra
        log.info("Gọi auth-service để tạo tài khoản với vai trò: {}", securityRole);
        InternalAccountRequest authRequest = new InternalAccountRequest();
        authRequest.setRole(securityRole); // 👈 Dùng role đã suy ra (không lấy từ hireRequest)

        // (Logic gọi RestTemplate exchange)
        HttpEntity<InternalAccountRequest> requestEntity = new HttpEntity<>(authRequest);
        ParameterizedTypeReference<ApiResponse<InternalAccountResponse>> responseType =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<ApiResponse<InternalAccountResponse>> responseEntity = restTemplate.exchange(
                authServiceUrl + "/api/auth/internal/create-account",
                HttpMethod.POST,
                requestEntity,
                responseType
        );
        ApiResponse<InternalAccountResponse> authResponse = responseEntity.getBody();
        String newStaffId = authResponse.getResult().getStaffId();
        log.info("Auth-service đã tạo account, trả về staffId: {}", newStaffId);


        // 4. Tách tên (firstname, lastname) từ fullName
        String firstname = applicant.getFullName();
        String lastname = "";
        if (applicant.getFullName().contains(" ")) {
            int lastSpace = applicant.getFullName().lastIndexOf(" ");
            firstname = applicant.getFullName().substring(0, lastSpace);
            lastname = applicant.getFullName().substring(lastSpace + 1);
        }

        // 5. (SỬA) Xây dựng StaffCreationRequest (Gán JobTitle và Role đã suy ra)
        StaffCreationRequest staffRequest = new StaffCreationRequest();
        staffRequest.setFirstname(firstname);
        staffRequest.setLastname(lastname);
        staffRequest.setShopId(hireRequest.getShopId());
        staffRequest.setSalary(hireRequest.getSalary());
        staffRequest.setHireDate(hireRequest.getHireDate());
        staffRequest.setDob(hireRequest.getDob());
        staffRequest.setGender(hireRequest.getGender());

        staffRequest.setRole(securityRole); // 👈 Gán Role đã suy ra
        staffRequest.setJobTitle(jobTitle); // 👈 Gán JobTitle đã suy ra

        // 6. Gọi StaffService (Truyền ID mới vào)
        StaffResponse staffResponse = staffService.createStaff(staffRequest, newStaffId);

        // 7. Cập nhật trạng thái ứng viên
        applicant.setStatus(ApplicantStatus.HIRED);
        applicantRepository.save(applicant);

        // 8. Trả về thông tin nhân viên
        return staffResponse;
    }
}