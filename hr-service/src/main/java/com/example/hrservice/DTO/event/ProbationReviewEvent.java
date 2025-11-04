package com.example.hrservice.DTO.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbationReviewEvent {
    private String staffId;
    private String staffName;
    private String shopId; // Để biết gửi thông báo cho Manager nào
    private LocalDate hireDate;
}