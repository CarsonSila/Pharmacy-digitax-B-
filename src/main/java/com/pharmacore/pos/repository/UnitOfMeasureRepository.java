package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Integer> {
    Optional<UnitOfMeasure> findByName(String name);
    Optional<UnitOfMeasure> findBySymbol(String symbol);
}

