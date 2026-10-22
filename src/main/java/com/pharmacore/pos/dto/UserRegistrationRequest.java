package com.pharmacore.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    private String username;
    private String fullName;
    private String role; // 'ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER'
    private String email;
    private String phone;
    private String pinCode; // 4-digit POS PIN
    private String password;
}

