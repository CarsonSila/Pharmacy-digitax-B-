package com.pharmacore.pos.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigiTaxResponse {

    private String status; // 'SUCCESS', 'FAILED', 'QUEUED'
    private String message;
    private DigiTaxData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DigiTaxData {
        private String internalInvoiceNumber;
        private String kraControlUnitNumber;
        private String kraInvoiceNumber;
        private String qrCodeUrl;
        private String qrCodeString;
        private TaxBreakdown taxBreakdown;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaxBreakdown {
        private BigDecimal vatableAmount;
        private BigDecimal vatAmount;
        private BigDecimal exemptAmount;
        private BigDecimal zeroRatedAmount;
    }
}

