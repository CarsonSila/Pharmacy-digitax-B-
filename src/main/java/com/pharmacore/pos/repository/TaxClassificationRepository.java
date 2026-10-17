package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.TaxClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TaxClassificationRepository extends JpaRepository<TaxClassification, Integer> {
    Optional<TaxClassification> findByClassCode(String classCode);
}

