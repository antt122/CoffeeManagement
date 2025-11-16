package com.example.coffeemanager.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsernameGenerationService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lấy số thứ tự tiếp theo từ sequence "USERNAME_SEQ".
     * Phải chạy trong một Transaction MỚI (PROPAGATION_REQUIRES_NEW)
     * để đảm bảo nó lấy được số ngay cả khi Transaction cha (của Listener) bị lỗi.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long getNextSequenceValue() {
        // Cú pháp chuẩn của Postgres/H2 để lấy giá trị tiếp theo
        Object result = entityManager.createNativeQuery("SELECT nextval('USERNAME_SEQ')").getSingleResult();

        if (result instanceof Number) {
            return ((Number) result).longValue();
        }
        return Long.valueOf(result.toString());
    }

    /**
     * Tạo username mới theo chuẩn 1000*
     */
    public String generateNextUsername() {
        Long nextId = getNextSequenceValue();
        return "1000" + nextId; // Ví dụ: 10001, 10002
    }
}