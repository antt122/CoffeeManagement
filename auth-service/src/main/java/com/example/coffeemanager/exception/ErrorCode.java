package com.example.coffeemanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    USER_EXISTED(1001, "Userexists", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1002, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST ),
    INVALID_PASSWORD(1003, "Password must be at least {min} characters long and contain at least 1 uppercase letter and 1 special character", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1004, "User not found", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1005, "Avail credentials", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1006, "Invalid token", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1007, "User does not exist",HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1008, "You not have permission", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1009, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNKNOWN_ERROR(9999, "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_NOT_EXISTED(1013, "Image not existed",HttpStatus.NOT_FOUND),
    INVALID_DOB(10010, "Date of birth must be at least {min}", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(10011, "Post not found", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_FUNDS(10012, "Don't have enough money to post", HttpStatus.PAYMENT_REQUIRED),
    TRANSACTION_NOT_FOUND(10013, "Error in payment process", HttpStatus.FORBIDDEN),
    POST_NOT_EXISTED(1010, "Post not existed",HttpStatus.NOT_FOUND),
    COMMENT_NOT_EXISTED(1011, "Comment not existed",HttpStatus.NOT_FOUND),
    CATEGORY_NOT_EXISTED(1012, "Categgory not existed",HttpStatus.NOT_FOUND),
    CATEGORY_DUPLICATED(1013, "Categgory is duplicated",HttpStatus.BAD_REQUEST),
    BAND_NOT_EXISTED(1014, "Band is not existed ",HttpStatus.NOT_FOUND),
    VENUE_NOT_EXISTED(1015, "Venue is not existed ",HttpStatus.NOT_FOUND),
    USER_ALREADY_IN_BAND(1016, "Band already in band",HttpStatus.BAD_REQUEST),
    BAND_MEMBER_NOT_EXISTED(1017, "Band member not existed",HttpStatus.NOT_FOUND),
    INVALID_CONTENT(1019, "The content of the article is not related to the topic of music", HttpStatus.BAD_REQUEST),
    SELF_CHAT_NOT_ALLOWED(1020, "You cannot chat to yourself", HttpStatus.BAD_REQUEST);


    private int code;
    private String message;
    private HttpStatusCode statuscode;
    ErrorCode(int code, String message, HttpStatusCode method) {
        this.code = code;
        this.message = message;
        this.statuscode = method;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public HttpStatusCode getStatuscode() {return statuscode;}

}
