package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.Product;
import com.pharmacore.pos.entity.ProductUnitConversion;
import com.pharmacore.pos.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductUnitConversionRepository extends JpaRepository<ProductUnitConversion, Integer> {
    List<ProductUnitConversion> findByProduct(Product product);
    Optional<ProductUnitConversion> findByProductAndUnit(Product product, UnitOfMeasure unit);
}
