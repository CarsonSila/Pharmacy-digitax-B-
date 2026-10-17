package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    /**
     * FEFO (First-Expired, First-Out) Retrieval:
     * Returns batches that are active, not quarantined, have available stock,
     * not expired, ordered strictly by expiry date ascending.
     */
    @Query("SELECT b FROM ProductBatch b WHERE b.product.productId = :productId " +
           "AND b.quantityAvailable > 0 " +
           "AND b.isQuarantined = false " +
           "AND b.expiryDate >= :currentDate " +
           "ORDER BY b.expiryDate ASC")
    List<ProductBatch> findAvailableBatchesFefo(
            @Param("productId") Integer productId,
            @Param("currentDate") LocalDate currentDate
    );

    List<ProductBatch> findByProductProductId(Integer productId);

    List<ProductBatch> findByExpiryDateBefore(LocalDate date);

    List<ProductBatch> findByExpiryDateBetween(LocalDate start, LocalDate end);
}

