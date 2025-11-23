package com.example.hrservice.DTO.request;
import lombok.Data;

@Data
public class BankAccountUpdateRequest {
    private String bankAccountNumber;
    private String bankName;
    private String bankBranch;
}