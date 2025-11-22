package com.example.hrservice.enums;

public enum LeaveType {
    ANNUAL_LEAVE,    // AL (Nghỉ có lương, bị trừ vào số dư)
    SICK_LEAVE,      // Nghỉ ốm (Có thể cần giấy tờ, không trừ)
    PUBLIC_HOLIDAY,  // Nghỉ lễ (Theo luật, không trừ)
    SPECIAL_LEAVE,   // Nghỉ đặc biệt (Tang lễ, tai nạn...)
    UNPAID_LEAVE     // Nghỉ không lương
}
