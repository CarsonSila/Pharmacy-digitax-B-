package com.pharmacore.pos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalInvoice {

    public enum FiscalStatus {
        QUEUED, PROCESSING, COMPLETED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fiscal_invoice_id")
    private Long fiscalInvoiceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnore
    private Order order;

    @Column(name = "branch_code", nullable = false, length = 50)
    @Builder.Default
    private String branchCode = "MAIN_BRANCH";

    @Column(name = "cashier_name", nullable = false, length = 150)
    private String cashierName;

    @Column(name = "buyer_pin", length = 30)
    private String buyerPin;

    @Column(name = "buyer_name", nullable = false, length = 150)
    @Builder.Default
    private String buyerName = "Walk-in Retail";

    @Column(name = "buyer_phone", length = 30)
    private String buyerPhone;

    @Column(name = "total_taxable_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalTaxableAmount;

    @Column(name = "total_exempt_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalExemptAmount;

    @Column(name = "total_zero_rated_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalZeroRatedAmount;

    @Column(name = "total_vat_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVatAmount;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "kra_control_unit_number", length = 100)
    private String kraControlUnitNumber;

    @Column(name = "kra_invoice_number", length = 100)
    private String kraInvoiceNumber;

    @Column(name = "qr_code_string", columnDefinition = "TEXT")
    private String qrCodeString;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiscal_status", nullable = false)
    @Builder.Default
    private FiscalStatus fiscalStatus = FiscalStatus.QUEUED;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "fiscalized_at")
    private LocalDateTime fiscalizedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

