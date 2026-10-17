package com.pharmacore.pos.dto;

import java.math.BigDecimal;

public class MpesaStkPushRequest {
    private String phoneNumber;
    private BigDecimal amount;
    private String orderReference;
    private String description;

    public MpesaStkPushRequest() {}

    public MpesaStkPushRequest(String phoneNumber, BigDecimal amount, String orderReference, String description) {
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.orderReference = orderReference;
        this.description = description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public void setOrderReference(String orderReference) {
        this.orderReference = orderReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

