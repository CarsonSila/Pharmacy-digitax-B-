package com.pharmacore.pos.dto;

import java.math.BigDecimal;

public class CreditNoteRequest {
    private Long orderId;
    private String reason;
    private boolean restockInventory = true;
    private String notes;

    public CreditNoteRequest() {}

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isRestockInventory() {
        return restockInventory;
    }

    public void setRestockInventory(boolean restockInventory) {
        this.restockInventory = restockInventory;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

