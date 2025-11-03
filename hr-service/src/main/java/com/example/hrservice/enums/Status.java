package com.example.hrservice.enums;


import lombok.Getter;

@Getter
public enum Status {
    /**
     * Nhân viên đang làm việc, tài khoản được kích hoạt.
     */
    ACTIVE("Đang làm việc"),

    /**
     * Nhân viên đang tạm nghỉ (nghỉ phép, nghỉ ốm,...)
     */
    ON_LEAVE("Nghỉ phép/Tạm nghỉ"),

    /**
     * Nhân viên đã nghỉ việc (tài khoản có thể bị vô hiệu hóa).
     */
    TERMINATED("Đã nghỉ việc"),

    /**
     * Nhân viên đang trong thời gian thử việc.
     */
    PROBATION("Thử việc");

    private final String description;

    Status(String description) {
        this.description = description;
    }
}
