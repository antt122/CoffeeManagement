package com.example.hrservice.DTO.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InternalAccountResponse {
    private String staffId;
    private String username;
}