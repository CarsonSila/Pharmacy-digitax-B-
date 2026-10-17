package com.pharmacore.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ZReportResponse {
    private String reportType; // "Z-REPORT" (daily close) or "X-REPORT" (midday audit)
    private LocalDate reportDate;
    private LocalDateTime generatedAt;
    private String pharmacyName;
    private String kraPin;
    private String branchCode;
    private String cashierName;

    // Sales Aggregates
    private int totalOrders;
    private int totalItemsSold;
    private BigDecimal grossSales;
    private BigDecimal netSales;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;

    // Tax Category Breakdown
    private BigDecimal vat16Gross;
    private BigDecimal vat16Net;
    private BigDecimal vat16Tax;
    private BigDecimal zeroRatedSales;
    private BigDecimal exemptSales;

    // Payment Tender Breakdown
    private BigDecimal cashTotal;
    private int cashCount;
    private BigDecimal mpesaTotal;
    private int mpesaCount;
    private BigDecimal cardTotal;
    private int cardCount;
    private BigDecimal insuranceTotal;
    private int insuranceCount;

    // DigiTax Fiscal Sync Status
    private int fiscalCompletedCount;
    private int fiscalQueuedCount;
    private int fiscalFailedCount;

    // Inventory Health Summary
    private int expiredBatchesCount;
    private int expiringSoonBatchesCount;
    private int lowStockProductsCount;

    // Formatted 80mm thermal receipt slip
    private String thermalReportText;

    public ZReportResponse() {}

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getKraPin() {
        return kraPin;
    }

    public void setKraPin(String kraPin) {
        this.kraPin = kraPin;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTotalItemsSold() {
        return totalItemsSold;
    }

    public void setTotalItemsSold(int totalItemsSold) {
        this.totalItemsSold = totalItemsSold;
    }

    public BigDecimal getGrossSales() {
        return grossSales;
    }

    public void setGrossSales(BigDecimal grossSales) {
        this.grossSales = grossSales;
    }

    public BigDecimal getNetSales() {
        return netSales;
    }

    public void setNetSales(BigDecimal netSales) {
        this.netSales = netSales;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }

    public BigDecimal getVat16Gross() {
        return vat16Gross;
    }

    public void setVat16Gross(BigDecimal vat16Gross) {
        this.vat16Gross = vat16Gross;
    }

    public BigDecimal getVat16Net() {
        return vat16Net;
    }

    public void setVat16Net(BigDecimal vat16Net) {
        this.vat16Net = vat16Net;
    }

    public BigDecimal getVat16Tax() {
        return vat16Tax;
    }

    public void setVat16Tax(BigDecimal vat16Tax) {
        this.vat16Tax = vat16Tax;
    }

    public BigDecimal getZeroRatedSales() {
        return zeroRatedSales;
    }

    public void setZeroRatedSales(BigDecimal zeroRatedSales) {
        this.zeroRatedSales = zeroRatedSales;
    }

    public BigDecimal getExemptSales() {
        return exemptSales;
    }

    public void setExemptSales(BigDecimal exemptSales) {
        this.exemptSales = exemptSales;
    }

    public BigDecimal getCashTotal() {
        return cashTotal;
    }

    public void setCashTotal(BigDecimal cashTotal) {
        this.cashTotal = cashTotal;
    }

    public int getCashCount() {
        return cashCount;
    }

    public void setCashCount(int cashCount) {
        this.cashCount = cashCount;
    }

    public BigDecimal getMpesaTotal() {
        return mpesaTotal;
    }

    public void setMpesaTotal(BigDecimal mpesaTotal) {
        this.mpesaTotal = mpesaTotal;
    }

    public int getMpesaCount() {
        return mpesaCount;
    }

    public void setMpesaCount(int mpesaCount) {
        this.mpesaCount = mpesaCount;
    }

    public BigDecimal getCardTotal() {
        return cardTotal;
    }

    public void setCardTotal(BigDecimal cardTotal) {
        this.cardTotal = cardTotal;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public BigDecimal getInsuranceTotal() {
        return insuranceTotal;
    }

    public void setInsuranceTotal(BigDecimal insuranceTotal) {
        this.insuranceTotal = insuranceTotal;
    }

    public int getInsuranceCount() {
        return insuranceCount;
    }

    public void setInsuranceCount(int insuranceCount) {
        this.insuranceCount = insuranceCount;
    }

    public int getFiscalCompletedCount() {
        return fiscalCompletedCount;
    }

    public void setFiscalCompletedCount(int fiscalCompletedCount) {
        this.fiscalCompletedCount = fiscalCompletedCount;
    }

    public int getFiscalQueuedCount() {
        return fiscalQueuedCount;
    }

    public void setFiscalQueuedCount(int fiscalQueuedCount) {
        this.fiscalQueuedCount = fiscalQueuedCount;
    }

    public int getFiscalFailedCount() {
        return fiscalFailedCount;
    }

    public void setFiscalFailedCount(int fiscalFailedCount) {
        this.fiscalFailedCount = fiscalFailedCount;
    }

    public int getExpiredBatchesCount() {
        return expiredBatchesCount;
    }

    public void setExpiredBatchesCount(int expiredBatchesCount) {
        this.expiredBatchesCount = expiredBatchesCount;
    }

    public int getExpiringSoonBatchesCount() {
        return expiringSoonBatchesCount;
    }

    public void setExpiringSoonBatchesCount(int expiringSoonBatchesCount) {
        this.expiringSoonBatchesCount = expiringSoonBatchesCount;
    }

    public int getLowStockProductsCount() {
        return lowStockProductsCount;
    }

    public void setLowStockProductsCount(int lowStockProductsCount) {
        this.lowStockProductsCount = lowStockProductsCount;
    }

    public String getThermalReportText() {
        return thermalReportText;
    }

    public void setThermalReportText(String thermalReportText) {
        this.thermalReportText = thermalReportText;
    }
}

