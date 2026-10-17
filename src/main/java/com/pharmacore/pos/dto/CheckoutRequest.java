package com.pharmacore.pos.dto;

import com.pharmacore.pos.entity.Order.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotNull(message = "Cashier ID is required")
    private Integer cashierId;

    private Integer customerId; // Optional: for registered loyalty/corporate customers
    private String buyerPin;    // Optional: for KRA eTIMS invoice (walk-ins can omit)
    private String buyerName;   // Default: 'Walk-in Retail'
    private String buyerPhone;

    private String prescriptionReference; // Optional POM prescription reference
    private String doctorName;
    private String patientName;

    @NotEmpty(message = "Cart cannot be empty")
    @Valid
    private List<CartItemRequest> items;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String paymentReference; // e.g. M-Pesa Transaction ID: SHK18928JD
    private BigDecimal amountTendered;
    private String notes;
}

