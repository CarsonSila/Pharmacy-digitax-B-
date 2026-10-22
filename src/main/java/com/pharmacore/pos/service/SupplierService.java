package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.CreateSupplierInvoiceRequest;
import com.pharmacore.pos.dto.CreateSupplierRequest;
import com.pharmacore.pos.dto.SupplierInvoiceItemDto;
import com.pharmacore.pos.entity.Product;
import com.pharmacore.pos.entity.ProductBatch;
import com.pharmacore.pos.entity.Supplier;
import com.pharmacore.pos.entity.SupplierInvoice;
import com.pharmacore.pos.entity.SupplierInvoiceItem;
import com.pharmacore.pos.repository.ProductBatchRepository;
import com.pharmacore.pos.repository.ProductRepository;
import com.pharmacore.pos.repository.SupplierInvoiceRepository;
import com.pharmacore.pos.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository supplierRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            ProductRepository productRepository,
            ProductBatchRepository productBatchRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public Supplier getSupplierById(Integer supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + supplierId));
    }

    @Transactional
    public Supplier createSupplier(CreateSupplierRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Supplier name is required.");
        }

        Supplier supplier = Supplier.builder()
                .name(req.getName().trim())
                .contactPerson(req.getContactPerson())
                .phone(req.getPhone())
                .email(req.getEmail())
                .kraPin(req.getKraPin())
                .address(req.getAddress())
                .paymentTerms(req.getPaymentTerms() != null ? req.getPaymentTerms() : "COD")
                .isActive(true)
                .build();

        Supplier saved = supplierRepository.save(supplier);
        log.info("Saved supplier: {} (ID: {})", saved.getName(), saved.getSupplierId());
        return saved;
    }

    public List<SupplierInvoice> getAllInvoices() {
        return supplierInvoiceRepository.findAllByOrderByCreatedAtDesc();
    }

    public SupplierInvoice getInvoiceById(Long invoiceId) {
        return supplierInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier Invoice not found with ID: " + invoiceId));
    }

    @Transactional
    public SupplierInvoice createSupplierInvoice(CreateSupplierInvoiceRequest req) {
        if (req.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier ID is required.");
        }
        if (req.getInvoiceNumber() == null || req.getInvoiceNumber().isBlank()) {
            throw new IllegalArgumentException("Invoice number is required.");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Invoice must contain at least one line item.");
        }

        Supplier supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + req.getSupplierId()));

        LocalDate invDate = req.getInvoiceDate() != null ? req.getInvoiceDate() : LocalDate.now();

        BigDecimal computedTotal = BigDecimal.ZERO;
        List<SupplierInvoiceItem> items = new ArrayList<>();

        SupplierInvoice invoice = SupplierInvoice.builder()
                .supplier(supplier)
                .invoiceNumber(req.getInvoiceNumber().trim())
                .invoiceDate(invDate)
                .totalAmount(BigDecimal.ZERO)
                .paymentStatus(req.getPaymentStatus() != null ? req.getPaymentStatus() : "PAID")
                .receivedBy(req.getReceivedBy() != null ? req.getReceivedBy() : "System Admin")
                .notes(req.getNotes())
                .build();

        for (SupplierInvoiceItemDto itemDto : req.getItems()) {
            if (itemDto.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required for each line item.");
            }
            if (itemDto.getQuantityReceived() == null || itemDto.getQuantityReceived() <= 0) {
                throw new IllegalArgumentException("Quantity received must be greater than zero.");
            }

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + itemDto.getProductId()));

            BigDecimal unitCost = itemDto.getUnitCost() != null ? itemDto.getUnitCost() : product.getCostPrice();
            int packSize = itemDto.getPackSize() != null && itemDto.getPackSize() > 0 ? itemDto.getPackSize() : 1;
            int totalBaseUnits = itemDto.getQuantityReceived() * packSize;

            BigDecimal lineTotal = unitCost.multiply(BigDecimal.valueOf(itemDto.getQuantityReceived()));
            computedTotal = computedTotal.add(lineTotal);

            String batchNumber = itemDto.getBatchNumber() != null && !itemDto.getBatchNumber().isBlank()
                    ? itemDto.getBatchNumber().trim()
                    : "GRN-" + System.currentTimeMillis();

            LocalDate expiryDate = itemDto.getExpiryDate() != null
                    ? itemDto.getExpiryDate()
                    : LocalDate.now().plusMonths(18);

            SupplierInvoiceItem invoiceItem = SupplierInvoiceItem.builder()
                    .supplierInvoice(invoice)
                    .product(product)
                    .batchNumber(batchNumber)
                    .manufacturingDate(itemDto.getManufacturingDate())
                    .expiryDate(expiryDate)
                    .quantityReceived(totalBaseUnits)
                    .packSize(packSize)
                    .unitCost(unitCost)
                    .totalCost(lineTotal)
                    .build();

            items.add(invoiceItem);

            // AUTO-STOCK UPDATE: Add or augment ProductBatch
            List<ProductBatch> existingBatches = productBatchRepository.findByProductProductId(product.getProductId());
            ProductBatch matchedBatch = existingBatches.stream()
                    .filter(b -> b.getBatchNumber().equalsIgnoreCase(batchNumber))
                    .findFirst()
                    .orElse(null);

            if (matchedBatch != null) {
                matchedBatch.setQuantityAvailable(matchedBatch.getQuantityAvailable() + totalBaseUnits);
                matchedBatch.setCostPrice(unitCost);
                matchedBatch.setExpiryDate(expiryDate);
                matchedBatch.setSupplierName(supplier.getName());
                productBatchRepository.save(matchedBatch);
                log.info("Augmented existing batch {} for product {} by {} units", batchNumber, product.getBrandName(), totalBaseUnits);
            } else {
                ProductBatch newBatch = ProductBatch.builder()
                        .product(product)
                        .batchNumber(batchNumber)
                        .manufacturingDate(itemDto.getManufacturingDate())
                        .expiryDate(expiryDate)
                        .costPrice(unitCost)
                        .quantityAvailable(totalBaseUnits)
                        .supplierName(supplier.getName())
                        .isQuarantined(false)
                        .build();
                productBatchRepository.save(newBatch);
                log.info("Created new batch {} for product {} with {} units", batchNumber, product.getBrandName(), totalBaseUnits);
            }

            // Update product's latest cost price
            product.setCostPrice(unitCost);
            productRepository.save(product);
        }

        invoice.setTotalAmount(req.getTotalAmount() != null && req.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
                ? req.getTotalAmount()
                : computedTotal);
        invoice.setItems(items);

        SupplierInvoice saved = supplierInvoiceRepository.save(invoice);
        log.info("Saved supplier invoice: #{} from {} with {} items, total: KES {}",
                saved.getInvoiceNumber(), supplier.getName(), items.size(), saved.getTotalAmount());
        return saved;
    }
}

