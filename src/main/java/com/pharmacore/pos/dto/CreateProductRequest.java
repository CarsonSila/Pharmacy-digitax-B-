package com.pharmacore.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateProductRequest {
    private String brandName;
    private String genericName;
    private String sku;
    private String barcode;
    private Integer categoryId;
    private Integer baseUnitId;
    private Integer taxClassId; // 1 = EXEMPT (Medicines), 2 = ZERO_RATED, 3 = VAT_16 (Cosmetics/supplements)
    private String hsCode;      // e.g. 3004.10.00
    private Boolean prescriptionRequired = false;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private Integer reorderLevel = 20;

    // Optional initial batch data (so pharmacist can add stock immediately)
    private String initialBatchNumber;
    private LocalDate initialExpiryDate;
    private Integer initialStockQuantity;
    private String supplierName;

    public CreateProductRequest() {}

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getBaseUnitId() {
        return baseUnitId;
    }

    public void setBaseUnitId(Integer baseUnitId) {
        this.baseUnitId = baseUnitId;
    }

    public Integer getTaxClassId() {
        return taxClassId;
    }

    public void setTaxClassId(Integer taxClassId) {
        this.taxClassId = taxClassId;
    }

    public String getHsCode() {
        return hsCode;
    }

    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }

    public Boolean getPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(Boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getInitialBatchNumber() {
        return initialBatchNumber;
    }

    public void setInitialBatchNumber(String initialBatchNumber) {
        this.initialBatchNumber = initialBatchNumber;
    }

    public LocalDate getInitialExpiryDate() {
        return initialExpiryDate;
    }

    public void setInitialExpiryDate(LocalDate initialExpiryDate) {
        this.initialExpiryDate = initialExpiryDate;
    }

    public Integer getInitialStockQuantity() {
        return initialStockQuantity;
    }

    public void setInitialStockQuantity(Integer initialStockQuantity) {
        this.initialStockQuantity = initialStockQuantity;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}

