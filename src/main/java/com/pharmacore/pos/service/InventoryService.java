package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.CreateBatchRequest;
import com.pharmacore.pos.dto.CreateProductRequest;
import com.pharmacore.pos.entity.*;
import com.pharmacore.pos.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final TaxClassificationRepository taxClassificationRepository;

    public InventoryService(
            ProductRepository productRepository,
            ProductBatchRepository productBatchRepository,
            CategoryRepository categoryRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            TaxClassificationRepository taxClassificationRepository) {
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
        this.categoryRepository = categoryRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.taxClassificationRepository = taxClassificationRepository;
    }

    @Transactional
    public Product createProduct(CreateProductRequest req) {
        if (req.getSku() != null && productRepository.findBySku(req.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU '" + req.getSku() + "' already exists.");
        }
        if (req.getBarcode() != null && productRepository.findByBarcode(req.getBarcode()).isPresent()) {
            throw new IllegalArgumentException("Product with Barcode '" + req.getBarcode() + "' already exists.");
        }

        Category category = categoryRepository.findById(req.getCategoryId() != null ? req.getCategoryId() : 1)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + req.getCategoryId()));

        UnitOfMeasure baseUnit = unitOfMeasureRepository.findById(req.getBaseUnitId() != null ? req.getBaseUnitId() : 1)
                .orElseThrow(() -> new IllegalArgumentException("Unit of Measure not found with ID: " + req.getBaseUnitId()));

        TaxClassification taxClassification = taxClassificationRepository.findById(req.getTaxClassId() != null ? req.getTaxClassId() : 1)
                .orElseThrow(() -> new IllegalArgumentException("Tax Classification not found with ID: " + req.getTaxClassId()));

        Product product = Product.builder()
                .brandName(req.getBrandName())
                .genericName(req.getGenericName())
                .sku(req.getSku() != null && !req.getSku().isBlank() ? req.getSku() : "SKU-" + System.currentTimeMillis())
                .barcode(req.getBarcode() != null && !req.getBarcode().isBlank() ? req.getBarcode() : null)
                .category(category)
                .baseUnit(baseUnit)
                .taxClassification(taxClassification)
                .hsCode(req.getHsCode() != null && !req.getHsCode().isBlank() ? req.getHsCode() : "3004.90.00")
                .prescriptionRequired(Boolean.TRUE.equals(req.getPrescriptionRequired()))
                .costPrice(req.getCostPrice() != null ? req.getCostPrice() : java.math.BigDecimal.ZERO)
                .sellingPrice(req.getSellingPrice())
                .reorderLevel(req.getReorderLevel() != null ? req.getReorderLevel() : 20)
                .isActive(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Created new pharmacy product: {} (ID: {})", savedProduct.getBrandName(), savedProduct.getProductId());

        // Create initial batch if stock details were supplied
        if (req.getInitialBatchNumber() != null && !req.getInitialBatchNumber().isBlank() && req.getInitialStockQuantity() != null && req.getInitialStockQuantity() > 0) {
            ProductBatch batch = ProductBatch.builder()
                    .product(savedProduct)
                    .batchNumber(req.getInitialBatchNumber())
                    .manufacturingDate(LocalDate.now().minusMonths(1))
                    .expiryDate(req.getInitialExpiryDate() != null ? req.getInitialExpiryDate() : LocalDate.now().plusYears(2))
                    .costPrice(req.getCostPrice() != null ? req.getCostPrice() : java.math.BigDecimal.ZERO)
                    .quantityAvailable(req.getInitialStockQuantity())
                    .supplierName(req.getSupplierName() != null ? req.getSupplierName() : "Local Pharmaceutical Distributor")
                    .isQuarantined(false)
                    .build();

            productBatchRepository.save(batch);
            log.info("Initialized initial batch {} with {} base units for product {}",
                    batch.getBatchNumber(), batch.getQuantityAvailable(), savedProduct.getBrandName());
        }

        return savedProduct;
    }

    @Transactional
    public ProductBatch addBatch(CreateBatchRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + req.getProductId()));

        ProductBatch batch = ProductBatch.builder()
                .product(product)
                .batchNumber(req.getBatchNumber())
                .manufacturingDate(req.getManufacturingDate() != null ? req.getManufacturingDate() : LocalDate.now())
                .expiryDate(req.getExpiryDate() != null ? req.getExpiryDate() : LocalDate.now().plusYears(2))
                .costPrice(req.getCostPrice() != null ? req.getCostPrice() : product.getCostPrice())
                .quantityAvailable(req.getQuantity() != null ? req.getQuantity() : 0)
                .supplierName(req.getSupplierName() != null ? req.getSupplierName() : "Consignment Shipment")
                .isQuarantined(false)
                .build();

        ProductBatch savedBatch = productBatchRepository.save(batch);
        log.info("Received consignment batch {} (Qty: {}) for product {}",
                savedBatch.getBatchNumber(), savedBatch.getQuantityAvailable(), product.getBrandName());

        return savedBatch;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("categories", categoryRepository.findAll());
        metadata.put("unitsOfMeasure", unitOfMeasureRepository.findAll());
        metadata.put("taxClassifications", taxClassificationRepository.findAll());
        return metadata;
    }
}

