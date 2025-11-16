package com.example.hrservice.enums;

import lombok.Getter;

@Getter // Dùng @Getter của Lombok
public enum Position {

    // Định nghĩa các giá trị
    BARISTA("Pha chế viên", "ROLE_STAFF"),
    CASHIER("Nhân viên Thu ngân", "ROLE_CASHIER"),
    SERVER("Nhân viên Phục vụ", "ROLE_STAFF"),
    SECURITY("Nhân viên Bảo vệ", "ROLE_STAFF"),
    MANAGER("Quản lý Cửa hàng", "ROLE_MANAGER");

    private final String jobTitle;
    private final String defaultSecurityRole;

    // Constructor của Enum
    Position(String jobTitle, String defaultSecurityRole) {
        this.jobTitle = jobTitle;
        this.defaultSecurityRole = defaultSecurityRole;
    }
}