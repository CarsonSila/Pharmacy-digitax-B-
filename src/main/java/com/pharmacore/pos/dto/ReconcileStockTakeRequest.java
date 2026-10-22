package com.pharmacore.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileStockTakeRequest {
    private String reconciledBy; // Manager username
    private String managerPin;    // Optional PIN verification
    private String notes;
}

