package com.pharmacore.pos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_take_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTakeItem {

    public enum AdjustmentReason {
        COUNT_DISCREPANCY,
        DAMAGED,
        EXPIRED,
        THEFT_OR_LOSS,
        SUPPLIER_MISMATCH,
        INITIAL_INVENTORY,
        NONE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private StockTakeSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id")
    private ProductBatch batch;

    @Column(name = "system_quantity", nullable = false)
    private Integer systemQuantity;

    @Column(name = "physical_quantity", nullable = false)
    private Integer physicalQuantity;

    @Column(name = "variance_quantity", nullable = false)
    private Integer varianceQuantity; // physical - system

    @Column(name = "unit_cost", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "variance_value", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal varianceValue = BigDecimal.ZERO; // varianceQuantity * unitCost

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_reason", length = 50)
    @Builder.Default
    private AdjustmentReason adjustmentReason = AdjustmentReason.COUNT_DISCREPANCY;

    @Column(length = 255)
    private String notes;
}

