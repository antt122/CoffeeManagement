package com.example.hrservice.service;


import com.example.hrservice.DTO.request.ApplicantHireRequest;
import com.example.hrservice.DTO.request.ApplicantRequest;
import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.response.ApplicantResponse;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.entity.Applicant;
import com.example.hrservice.enums.ApplicantStatus;
import com.example.hrservice.repository.ApplicantRepository;
import jakarta.annotation.PostConstruct; // 👈 Import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 👈 Giữ nguyên
@Slf4j
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final ModelMapper modelMapper;
    private final StaffService staffService;

    // (Tạm thời lưu file vào thư mục "uploads" - trong Docker, đây phải là 1 Volume)
    private final Path rootLocation = Paths.get("uploads");

    /**
     * Dùng @PostConstruct để khởi tạo thư mục sau khi Service được tạo
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage location", e);
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    // XÓA CONSTRUCTOR THỦ CÔNG MÀ TÔI ĐÃ THÊM TRƯỚC ĐÓ

    /**
     * Sửa lại hàm này để nhận cả (MultipartFile) và (Request DTO)
     */
    public ApplicantResponse submitApplication(ApplicantRequest request, MultipartFile cvFile) {
        log.info("Nhận đơn ứng tuyển mới cho vị trí: {}", request.getPositionApplied());

        // 1. Xử lý File Upload
        if (cvFile == null || cvFile.isEmpty()) {
            throw new RuntimeException("CV file is required");
        }

        // Tạo tên file duy nhất (ví dụ: 123e4567-e89b-12d3-a456-426614174000.pdf)
        // 👈 Sửa lỗi 'google'
        String extension = com.google.common.io.Files.getFileExtension(cvFile.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

        String cvPath;
        try {
            // Lưu file vào thư mục (ví dụ: "uploads/123e4567.pdf")
            Path destinationFile = this.rootLocation.resolve(uniqueFileName);
            Files.copy(cvFile.getInputStream(), destinationFile);

            // 2. Chỉ lưu đường dẫn (path) vào DB
            cvPath = destinationFile.toString(); // 👈 Sửa lỗi 'toString()' (nếu có)

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }

        // 3. Map DTO -> Entity
        Applicant applicant = modelMapper.map(request, Applicant.class);
        applicant.setStatus(ApplicantStatus.PENDING);
        applicant.setCvUrl(cvPath); // 👈 Sửa lỗi 'setCvUrl'

        Applicant savedApplicant = applicantRepository.save(applicant);

        return modelMapper.map(savedApplicant, ApplicantResponse.class);
    }
    @Transactional(readOnly = true )
    public List<ApplicantResponse> getApplicantsByStatus(ApplicantStatus status) {
        log.info("Đang tìm ứng viên với trạng thái: {}", status);

        // 1. Gọi Repository
        List<Applicant> applicants = applicantRepository.findByStatus(status);

        // 2. Dùng ModelMapper để chuyển đổi List<Entity> sang List<DTO>
        return applicants.stream()
                .map(applicant -> modelMapper.map(applicant, ApplicantResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffResponse hireApplicant(String applicantId, ApplicantHireRequest hireRequest) {

        // 1. Tìm ứng viên (Nơi chứa Tên, Email, SĐT)
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantId));

        if (applicant.getStatus() == ApplicantStatus.HIRED) {
            throw new RuntimeException("Applicant is already hired.");
        }

        // 2. Tách tên (firstname, lastname) từ fullName
        // (Đây là logic ví dụ, bạn có thể làm phức tạp hơn)
        String firstname = applicant.getFullName();
        String lastname = "";
        if (applicant.getFullName().contains(" ")) {
            int lastSpace = applicant.getFullName().lastIndexOf(" ");
            firstname = applicant.getFullName().substring(0, lastSpace);
            lastname = applicant.getFullName().substring(lastSpace + 1);
        }

        // 3. (QUAN TRỌNG) Xây dựng StaffCreationRequest từ 2 nguồn
        StaffCreationRequest staffRequest = new StaffCreationRequest();

        // --- Lấy từ Applicant (dữ liệu cũ) ---
        staffRequest.setFirstname(firstname);
        staffRequest.setLastname(lastname);
        // (Bạn cũng có thể map email, phone nếu Entity Staff có)

        // --- Lấy từ HireRequest (dữ liệu Manager nhập) ---
        staffRequest.setUsername(hireRequest.getUsername());
        staffRequest.setPassword(hireRequest.getPassword());
        staffRequest.setRole(hireRequest.getRole());
        staffRequest.setShopId(hireRequest.getShopId());
        staffRequest.setSalary(hireRequest.getSalary());
        staffRequest.setHireDate(hireRequest.getHireDate());
        staffRequest.setDob(hireRequest.getDob());
        staffRequest.setGender(hireRequest.getGender());

        // 4. Gọi StaffService (Giống hệt lúc trước)
        log.info("Hiring applicant {}. Creating staff record...", applicant.getFullName());
        StaffResponse staffResponse = staffService.createStaff(staffRequest);
        log.info("Staff record created with ID: {}", staffResponse.getStaffId());

        // 5. Cập nhật trạng thái ứng viên
        applicant.setStatus(ApplicantStatus.HIRED);
        applicantRepository.save(applicant);

        // 6. Trả về thông tin nhân viên (đã dùng ModelMapper)
        return staffResponse;
    }
}