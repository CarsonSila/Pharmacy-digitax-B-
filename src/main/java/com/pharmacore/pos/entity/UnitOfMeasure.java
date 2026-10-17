package com.pharmacore.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "units_of_measure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitOfMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Integer unitId;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // 'Box', 'Blister', 'Tablet', 'Bottle'

    @Column(nullable = false, length = 20)
    private String symbol; // 'box', 'strip', 'tab', 'btl'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

