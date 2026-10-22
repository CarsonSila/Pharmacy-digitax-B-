package com.pharmacore.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTakeCountRequest {
    private Long sessionId;
    private List<ItemCount> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemCount {
        private Integer productId;
        private Long batchId;
        private Integer physicalQuantity;
        private String adjustmentReason;
        private String notes;
    }
}

