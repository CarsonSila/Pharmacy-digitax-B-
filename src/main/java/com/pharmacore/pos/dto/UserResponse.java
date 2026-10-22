package com.pharmacore.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer userId;
    private String username;
    private String fullName;
    private String role;
    private String email;
    private String phone;
    private Boolean isActive;
    private Boolean hasPin;
}

