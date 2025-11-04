package com.example.coffeemanager.repository;

import com.example.coffeemanager.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    /**
     * Tìm kiếm một tài khoản bằng username (tên đăng nhập).
     */
    Optional<Account> findByUsername(String username);
    Optional<Account> findByStaffId(String staffId);
}
