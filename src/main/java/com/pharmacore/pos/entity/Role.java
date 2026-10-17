package com.pharmacore.pos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // 'ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER'

    @Column(length = 255)
    private String description;
}

