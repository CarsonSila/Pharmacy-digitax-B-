package com.pharmacore.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierRequest {
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String kraPin;
    private String address;
    private String paymentTerms;
}

