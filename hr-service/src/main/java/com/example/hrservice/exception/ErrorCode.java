package com.example.hrservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    // Lỗi chung
    UNKNOWN_ERROR(9999, "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1008, "You do not have permission", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1009, "Unauthenticated", HttpStatus.UNAUTHORIZED),

    // Lỗi nghiệp vụ (HR)
    STAFF_NOT_FOUND(2001, "Staff not found", HttpStatus.NOT_FOUND),
    APPLICANT_NOT_FOUND(2002, "Applicant not found", HttpStatus.NOT_FOUND),
    APPLICANT_ALREADY_HIRED(2003, "Applicant is already hired", HttpStatus.BAD_REQUEST),
    FILE_STORAGE_FAILED(2004, "Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_FOUND(2005, "Role not found in Auth Service", HttpStatus.BAD_REQUEST),
    FILE_IS_REQUIRED(2006, "File (e.g., CV) is required", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1010, "Invalid or missing token claims", HttpStatus.UNAUTHORIZED);

    private int code;
    private String message;
    private HttpStatusCode statuscode;

    ErrorCode(int code, String message, HttpStatusCode method) {
        this.code = code;
        this.message = message;
        this.statuscode = method;
    }

    // Getters
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public HttpStatusCode getStatuscode() {return statuscode;}
}