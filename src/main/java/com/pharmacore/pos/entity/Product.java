package com.pharmacore.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(unique = true, length = 100)
    private String barcode;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "generic_name")
    private String genericName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "base_unit_id", nullable = false)
    private UnitOfMeasure baseUnit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tax_class_id", nullable = false)
    private TaxClassification taxClassification;

    @Column(name = "hs_code", nullable = false, length = 50)
    private String hsCode; // e.g., '3004.10.00'

    @Column(name = "prescription_required")
    @Builder.Default
    private Boolean prescriptionRequired = false;

    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 20;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductUnitConversion> unitConversions = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductBatch> batches = new ArrayList<>();

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

