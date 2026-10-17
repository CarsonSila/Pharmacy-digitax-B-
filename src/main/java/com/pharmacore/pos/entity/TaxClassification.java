package com.pharmacore.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_classifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_class_id")
    private Integer taxClassId;

    @Column(name = "class_code", nullable = false, unique = true, length = 30)
    private String classCode; // 'EXEMPT', 'ZERO_RATED', 'VAT_16'

    @Column(name = "tax_name", nullable = false, length = 100)
    private String taxName;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate; // 0.00 or 16.00

    @Column(name = "kra_tax_code", nullable = false, length = 10)
    private String kraTaxCode; // 'A', 'B', 'E'

    @Column(name = "legal_description", length = 255)
    private String legalDescription;
}

