package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findByBarcode(String barcode);

    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.genericName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " p.barcode = :keyword OR p.sku = :keyword)")
    List<Product> searchActiveProducts(@Param("keyword") String keyword);
}

