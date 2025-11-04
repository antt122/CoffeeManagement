package com.example.coffeemanager.DTO.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateEvent {
    private String staffId;
    private String newRole;
    private Boolean enabled;
}