package com.example.hrservice.controller;


import com.example.hrservice.DTO.request.ApplicantRequest;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.DTO.response.ApplicantResponse;
import com.example.hrservice.enums.Position;
import com.example.hrservice.service.ApplicantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/hr/public") // 👈 Endpoint công khai
@RequiredArgsConstructor
public class ApplicantController {

    private final ApplicantService applicantService;



        @PostMapping(value = "/apply", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        public ApiResponse<ApplicantResponse> submitApplication(

                // 👇 THAY VÌ DÙNG @RequestPart("data"), HÃY DÙNG @RequestParam CHO TỪNG TRƯỜNG
                @RequestParam("fullName") String fullName,
                @RequestParam("email") String email,
                @RequestParam("phone") String phone,
                @RequestParam("positionApplied") Position positionApplied,
                @RequestParam(value = "coverLetter", required = false) String coverLetter,

                // File vẫn dùng @RequestPart
                @RequestPart("cvFile") MultipartFile cvFile
        ) {

            // Tạo lại DTO bằng tay
            ApplicantRequest request = new ApplicantRequest();
            request.setFullName(fullName);
            request.setEmail(email);
            request.setPhone(phone);
            request.setPositionApplied(positionApplied);
            request.setCoverLetter(coverLetter);

            ApplicantResponse response = applicantService.submitApplication(request, cvFile);

            return ApiResponse.<ApplicantResponse>builder()
                    .result(response)
                    .build();
        }
    }

