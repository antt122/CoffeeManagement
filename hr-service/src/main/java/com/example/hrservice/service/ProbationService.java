package com.example.hrservice.service;

import com.example.hrservice.DTO.event.ProbationReviewEvent;
import com.example.hrservice.config.RabbitMQConfig;
import com.example.hrservice.entity.Staff;
import com.example.hrservice.enums.Status;
import com.example.hrservice.repository.StaffRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProbationService {

    private final StaffRepository staffRepository;
    private final RabbitTemplate rabbitTemplate;

    // Thời gian thử việc (ví dụ: 90 ngày)
    private static final int PROBATION_DAYS = 90;

    /**
     * Tự động chạy hàng ngày lúc 2 giờ sáng.
     * (cron = "giây phút giờ ngày tháng ngày_trong_tuần")
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkProbationPeriods() {
        log.info("Bắt đầu tác vụ hàng đêm: Kiểm tra hạn thử việc...");

        // 1. Tính mốc thời gian (ví dụ: 90 ngày trước)
        LocalDate cutOffDate = LocalDate.now().minusDays(PROBATION_DAYS);

        // 2. Tìm các nhân viên đạt yêu cầu
        List<Staff> staffToReview = staffRepository
                .findByStatusAndHireDateBeforeAndProbationNotifiedFalse(
                        Status.PROBATION,
                        cutOffDate
                );

        if (staffToReview.isEmpty()) {
            log.info("Không có nhân viên nào đến hạn xem xét thử việc.");
            return;
        }

        log.info("Phát hiện {} nhân viên cần xem xét thử việc.", staffToReview.size());

        for (Staff staff : staffToReview) {
            // 3. Chuẩn bị tin nhắn
            ProbationReviewEvent event = ProbationReviewEvent.builder()
                    .staffId(staff.getId())
                    .staffName(staff.getFirstname() + " " + staff.getLastname())
                    .shopId(staff.getShopId())
                    .hireDate(staff.getHireDate())
                    .build();

            // 4. Gửi tin nhắn đến RabbitMQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE, // Exchange mới
                    RabbitMQConfig.PROBATION_ROUTING_KEY, // Key mới
                    event
            );
            log.info("Đã gửi thông báo xem xét cho Staff ID: {}", staff.getId());

            // 5. Đánh dấu là đã thông báo
            staff.setProbationNotified(true);
            staffRepository.save(staff);
        }
        log.info("Hoàn tất tác vụ kiểm tra thử việc.");
    }
}
