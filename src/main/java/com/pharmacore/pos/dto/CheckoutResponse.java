package com.pharmacore.pos.dto;

import com.pharmacore.pos.entity.FiscalInvoice.FiscalStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {

    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String cashierName;
    private String customerName;

    // Monetary & Tax Breakdown
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal vatableNet;
    private BigDecimal vatAmount;
    private BigDecimal exemptAmount;
    private BigDecimal zeroRatedAmount;
    private BigDecimal totalAmount;

    // Payment details
    private String paymentMethod;
    private String paymentReference;

    // KRA eTIMS / DigiTax Status
    private FiscalStatus fiscalStatus;
    private String kraControlUnitNumber;
    private String kraInvoiceNumber;
    private String qrCodeString;
    private String qrCodeUrl;
    private String fiscalMessage;

    // Pre-formatted 80mm thermal receipt text
    private String formattedReceipt;

    private List<CheckoutItemDetail> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutItemDetail {
        private String productName;
        private String unitName;
        private Integer quantity;
        private String batchNumber;
        private String expiryDate;
        private BigDecimal unitPrice;
        private String taxCategory;
        private BigDecimal taxAmount;
        private BigDecimal lineTotal;
    }
}

