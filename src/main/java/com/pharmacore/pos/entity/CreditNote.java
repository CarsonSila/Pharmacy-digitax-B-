package com.pharmacore.pos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_notes")
public class CreditNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_note_id")
    private Long creditNoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_order_id", nullable = false)
    private Order originalOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_fiscal_invoice_id", nullable = false)
    private FiscalInvoice originalFiscalInvoice;

    @Column(name = "credit_note_number", nullable = false, unique = true, length = 50)
    private String creditNoteNumber;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "tax_refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxRefundAmount;

    @Column(name = "kra_credit_note_number", length = 100)
    private String kraCreditNoteNumber;

    @Column(name = "kra_qr_code_string", columnDefinition = "TEXT")
    private String kraQrCodeString;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CreditNoteStatus status = CreditNoteStatus.QUEUED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum CreditNoteStatus {
        QUEUED,
        COMPLETED,
        FAILED
    }

    public CreditNote() {}

    public Long getCreditNoteId() {
        return creditNoteId;
    }

    public void setCreditNoteId(Long creditNoteId) {
        this.creditNoteId = creditNoteId;
    }

    public Order getOriginalOrder() {
        return originalOrder;
    }

    public void setOriginalOrder(Order originalOrder) {
        this.originalOrder = originalOrder;
    }

    public FiscalInvoice getOriginalFiscalInvoice() {
        return originalFiscalInvoice;
    }

    public void setOriginalFiscalInvoice(FiscalInvoice originalFiscalInvoice) {
        this.originalFiscalInvoice = originalFiscalInvoice;
    }

    public String getCreditNoteNumber() {
        return creditNoteNumber;
    }

    public void setCreditNoteNumber(String creditNoteNumber) {
        this.creditNoteNumber = creditNoteNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getTaxRefundAmount() {
        return taxRefundAmount;
    }

    public void setTaxRefundAmount(BigDecimal taxRefundAmount) {
        this.taxRefundAmount = taxRefundAmount;
    }

    public String getKraCreditNoteNumber() {
        return kraCreditNoteNumber;
    }

    public void setKraCreditNoteNumber(String kraCreditNoteNumber) {
        this.kraCreditNoteNumber = kraCreditNoteNumber;
    }

    public String getKraQrCodeString() {
        return kraQrCodeString;
    }

    public void setKraQrCodeString(String kraQrCodeString) {
        this.kraQrCodeString = kraQrCodeString;
    }

    public CreditNoteStatus getStatus() {
        return status;
    }

    public void setStatus(CreditNoteStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

