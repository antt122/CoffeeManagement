package com.example.coffeemanager.entity;


import jakarta.persistence.*;

/**
 * Entity này KHÔNG dùng để lưu dữ liệu,
 * mà chỉ dùng để Hibernate @SequenceGenerator
 * tự động tạo ra "USERNAME_SEQ" trong Postgres.
 */
@Entity
@Table(name = "username_sequence")
public class UsernameSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "username_seq_gen")
    @SequenceGenerator(
            name = "username_seq_gen",
            sequenceName = "USERNAME_SEQ", // Tên sequence trong DB
            initialValue = 1, // Bắt đầu từ 1
            allocationSize = 1 // Mỗi lần gọi tăng 1
    )
    private Long id;
}