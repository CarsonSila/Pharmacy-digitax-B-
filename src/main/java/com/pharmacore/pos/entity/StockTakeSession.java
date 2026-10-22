package com.pharmacore.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_take_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTakeSession {

    public enum Status {
        IN_PROGRESS, RECONCILED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "session_code", nullable = false, unique = true, length = 50)
    private String sessionCode; // e.g. STK-20261022-001

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.IN_PROGRESS;

    @Column(name = "initiated_by", length = 100)
    private String initiatedBy;

    @Column(name = "reconciled_by", length = 100)
    private String reconciledBy;

    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<StockTakeItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }
}

