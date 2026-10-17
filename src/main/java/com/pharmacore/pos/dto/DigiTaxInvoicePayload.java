package com.pharmacore.pos.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigiTaxInvoicePayload {

    private String branchCode;
    private String cashier;
    private BuyerInfo buyer;
    private List<ItemInfo> items;
    private PaymentInfo payment;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BuyerInfo {
        private String pin;
        private String name;
        private String phone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemInfo {
        private String code;
        private String name;
        private String hsCode;
        private String taxType; // 'VAT_16', 'EXEMPT', 'ZERO_RATED'
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private String method;
        private String reference;
        private BigDecimal amountPaid;
    }
}

