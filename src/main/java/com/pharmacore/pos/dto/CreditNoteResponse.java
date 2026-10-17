package com.pharmacore.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditNoteResponse {
    private Long creditNoteId;
    private String creditNoteNumber;
    private String originalOrderNumber;
    private String originalKraInvoiceNumber;
    private BigDecimal refundAmount;
    private BigDecimal taxRefundAmount;
    private String kraCreditNoteNumber;
    private String kraQrCodeString;
    private String status;
    private LocalDateTime createdAt;
    private String message;

    public CreditNoteResponse() {}

    public Long getCreditNoteId() {
        return creditNoteId;
    }

    public void setCreditNoteId(Long creditNoteId) {
        this.creditNoteId = creditNoteId;
    }

    public String getCreditNoteNumber() {
        return creditNoteNumber;
    }

    public void setCreditNoteNumber(String creditNoteNumber) {
        this.creditNoteNumber = creditNoteNumber;
    }

    public String getOriginalOrderNumber() {
        return originalOrderNumber;
    }

    public void setOriginalOrderNumber(String originalOrderNumber) {
        this.originalOrderNumber = originalOrderNumber;
    }

    public String getOriginalKraInvoiceNumber() {
        return originalKraInvoiceNumber;
    }

    public void setOriginalKraInvoiceNumber(String originalKraInvoiceNumber) {
        this.originalKraInvoiceNumber = originalKraInvoiceNumber;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

