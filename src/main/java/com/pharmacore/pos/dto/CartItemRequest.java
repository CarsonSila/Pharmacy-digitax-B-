package com.pharmacore.pos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequest {

    @NotNull(message = "Product ID is required")
    private Integer productId;

    private Integer unitId; // Optional: If null, uses base unit (e.g. tablet)

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal discount; // Optional line discount

    private BigDecimal customUnitPrice; // Optional: Cashier / Pharmacist counter price override (e.g. 86 -> 90)

    private BigDecimal unitPrice; // Optional: Alias for custom unit price from POS cart
}

