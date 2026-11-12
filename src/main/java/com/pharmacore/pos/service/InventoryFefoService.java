package com.pharmacore.pos.service;

import com.pharmacore.pos.entity.Product;
import com.pharmacore.pos.entity.ProductBatch;
import com.pharmacore.pos.entity.ProductUnitConversion;
import com.pharmacore.pos.entity.UnitOfMeasure;
import com.pharmacore.pos.repository.ProductBatchRepository;
import com.pharmacore.pos.repository.ProductRepository;
import com.pharmacore.pos.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryFefoService {

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public record AllocatedBatchAllocation(
            ProductBatch batch,
            UnitOfMeasure unit,
            int quantitySoldInUnit,
            int baseUnitsDeducted,
            BigDecimal unitPrice
    ) {}

    /**
     * Resolves product unit conversion factor and unit price.
     */
    public record UnitPriceAndFactor(int conversionFactor, BigDecimal unitPrice, UnitOfMeasure unit) {}

    public UnitPriceAndFactor resolveUnitAndPrice(Product product, Integer unitId, String saleType) {
        // If saleType is explicitly "P" (Piece)
        if ("P".equalsIgnoreCase(saleType)) {
            return new UnitPriceAndFactor(1, product.getEffectivePiecePrice(), product.getBaseUnit());
        }

        // If saleType is explicitly "W" (Whole)
        if ("W".equalsIgnoreCase(saleType)) {
            int factor = product.getPackSize() != null && product.getPackSize() > 0 ? product.getPackSize() : 1;
            return new UnitPriceAndFactor(factor, product.getSellingPrice(), product.getBaseUnit());
        }

        if (unitId == null || unitId.equals(product.getBaseUnit().getUnitId())) {
            return new UnitPriceAndFactor(1, product.getEffectivePiecePrice(), product.getBaseUnit());
        }

        // Look for unit conversion rule (e.g. 1 strip = 10 tabs, 1 box = 100 tabs)
        return product.getUnitConversions().stream()
                .filter(c -> c.getUnit().getUnitId().equals(unitId))
                .findFirst()
                .map(c -> new UnitPriceAndFactor(c.getConversionFactor(), c.getSellingPrice(), c.getUnit()))
                .orElseGet(() -> {
                    UnitOfMeasure requestedUnit = unitOfMeasureRepository.findById(unitId)
                            .orElse(product.getBaseUnit());
                    return new UnitPriceAndFactor(1, product.getEffectivePiecePrice(), requestedUnit);
                });
    }

    public UnitPriceAndFactor resolveUnitAndPrice(Product product, Integer unitId) {
        return resolveUnitAndPrice(product, unitId, null);
    }

    /**
     * Allocates inventory using FEFO (First-Expired, First-Out).
     * Automatically picks the batch that expires earliest.
     */
    @Transactional
    public AllocatedBatchAllocation allocateBatchFefo(Integer productId, Integer unitId, String saleType, int quantityRequested) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        UnitPriceAndFactor unitInfo = resolveUnitAndPrice(product, unitId, saleType);
        int totalBaseUnitsRequired = quantityRequested * unitInfo.conversionFactor();

        LocalDate today = LocalDate.now();
        List<ProductBatch> availableBatches = productBatchRepository.findAvailableBatchesFefo(productId, today);

        if (availableBatches.isEmpty()) {
            throw new IllegalStateException("Out of stock or expired for product: " + product.getBrandName());
        }

        // Pick the earliest expiring batch that has enough stock
        ProductBatch selectedBatch = null;
        for (ProductBatch batch : availableBatches) {
            if (batch.getQuantityAvailable() >= totalBaseUnitsRequired) {
                selectedBatch = batch;
                break;
            }
        }

        // Fallback: If no single batch has full quantity, take from the earliest batch available
        if (selectedBatch == null) {
            selectedBatch = availableBatches.get(0);
            if (selectedBatch.getQuantityAvailable() < totalBaseUnitsRequired) {
                throw new IllegalStateException(String.format(
                        "Insufficient stock for %s in batch %s (Requested %d base units, available: %d)",
                        product.getBrandName(), selectedBatch.getBatchNumber(),
                        totalBaseUnitsRequired, selectedBatch.getQuantityAvailable()
                ));
            }
        }

        // Deduct inventory in base units (atomic in transaction)
        selectedBatch.setQuantityAvailable(selectedBatch.getQuantityAvailable() - totalBaseUnitsRequired);
        productBatchRepository.save(selectedBatch);

        log.info("FEFO Allocated {} base units for {} from batch {} (Expires: {})",
                totalBaseUnitsRequired, product.getBrandName(), selectedBatch.getBatchNumber(), selectedBatch.getExpiryDate());

        return new AllocatedBatchAllocation(
                selectedBatch,
                unitInfo.unit(),
                quantityRequested,
                totalBaseUnitsRequired,
                unitInfo.unitPrice()
        );
    }

    @Transactional
    public AllocatedBatchAllocation allocateBatchFefo(Integer productId, Integer unitId, int quantityRequested) {
        return allocateBatchFefo(productId, unitId, null, quantityRequested);
    }
}

