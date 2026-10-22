package com.pharmacore.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierInvoiceRequest {
    private Integer supplierId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
    private String paymentStatus; // PAID, PENDING, PARTIAL
    private String receivedBy;
    private String notes;
    private List<SupplierInvoiceItemDto> items;
}

