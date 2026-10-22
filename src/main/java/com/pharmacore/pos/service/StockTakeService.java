package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.CreateStockTakeRequest;
import com.pharmacore.pos.dto.ReconcileStockTakeRequest;
import com.pharmacore.pos.dto.StockTakeCountRequest;
import com.pharmacore.pos.entity.Product;
import com.pharmacore.pos.entity.ProductBatch;
import com.pharmacore.pos.entity.StockTakeItem;
import com.pharmacore.pos.entity.StockTakeSession;
import com.pharmacore.pos.repository.ProductBatchRepository;
import com.pharmacore.pos.repository.ProductRepository;
import com.pharmacore.pos.repository.StockTakeItemRepository;
import com.pharmacore.pos.repository.StockTakeSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockTakeService {

    private static final Logger log = LoggerFactory.getLogger(StockTakeService.class);

    private final StockTakeSessionRepository sessionRepository;
    private final StockTakeItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final UserService userService;

    public StockTakeService(
            StockTakeSessionRepository sessionRepository,
            StockTakeItemRepository itemRepository,
            ProductRepository productRepository,
            ProductBatchRepository productBatchRepository,
            UserService userService) {
        this.sessionRepository = sessionRepository;
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
        this.userService = userService;
    }

    public List<StockTakeSession> getAllSessions() {
        return sessionRepository.findAllByOrderByStartedAtDesc();
    }

    public StockTakeSession getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Stock take session not found: " + sessionId));
    }

    @Transactional
    public StockTakeSession startSession(CreateStockTakeRequest req) {
        // Check if an in-progress session already exists
        Optional<StockTakeSession> inProgress = sessionRepository.findFirstByStatusOrderByStartedAtDesc(StockTakeSession.Status.IN_PROGRESS);
        if (inProgress.isPresent()) {
            return inProgress.get();
        }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sessionCode = "STK-" + dateStr + "-" + String.format("%03d", (int) (Math.random() * 900) + 100);

        StockTakeSession session = StockTakeSession.builder()
                .sessionCode(sessionCode)
                .status(StockTakeSession.Status.IN_PROGRESS)
                .initiatedBy(req.getInitiatedBy() != null ? req.getInitiatedBy() : "Store Pharmacist")
                .notes(req.getNotes())
                .items(new ArrayList<>())
                .build();

        StockTakeSession savedSession = sessionRepository.save(session);

        List<Product> products = productRepository.findAll();
        List<StockTakeItem> items = new ArrayList<>();

        for (Product product : products) {
            List<ProductBatch> batches = productBatchRepository.findByProductProductId(product.getProductId());
            int totalSystemQty = batches.stream().mapToInt(ProductBatch::getQuantityAvailable).sum();

            // Default to latest batch or null
            ProductBatch primaryBatch = batches.isEmpty() ? null : batches.get(0);

            StockTakeItem item = StockTakeItem.builder()
                    .session(savedSession)
                    .product(product)
                    .batch(primaryBatch)
                    .systemQuantity(totalSystemQty)
                    .physicalQuantity(totalSystemQty) // Default to system until verified
                    .varianceQuantity(0)
                    .unitCost(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO)
                    .varianceValue(BigDecimal.ZERO)
                    .adjustmentReason(StockTakeItem.AdjustmentReason.NONE)
                    .build();

            items.add(item);
        }

        itemRepository.saveAll(items);
        savedSession.setItems(items);

        log.info("Started new stock take session {} with {} items", sessionCode, items.size());
        return savedSession;
    }

    @Transactional
    public StockTakeSession recordCounts(StockTakeCountRequest req) {
        StockTakeSession session = getSessionById(req.getSessionId());
        if (session.getStatus() != StockTakeSession.Status.IN_PROGRESS) {
            throw new IllegalStateException("Cannot update counts for session with status: " + session.getStatus());
        }

        List<StockTakeItem> sessionItems = itemRepository.findBySessionSessionId(session.getSessionId());

        if (req.getItems() != null) {
            for (StockTakeCountRequest.ItemCount count : req.getItems()) {
                StockTakeItem matched = sessionItems.stream()
                        .filter(i -> i.getProduct().getProductId().equals(count.getProductId()))
                        .findFirst()
                        .orElse(null);

                if (matched != null && count.getPhysicalQuantity() != null) {
                    matched.setPhysicalQuantity(count.getPhysicalQuantity());
                    int variance = count.getPhysicalQuantity() - matched.getSystemQuantity();
                    matched.setVarianceQuantity(variance);
                    BigDecimal unitCost = matched.getUnitCost() != null ? matched.getUnitCost() : BigDecimal.ZERO;
                    matched.setVarianceValue(unitCost.multiply(BigDecimal.valueOf(variance)));

                    if (count.getAdjustmentReason() != null) {
                        try {
                            matched.setAdjustmentReason(StockTakeItem.AdjustmentReason.valueOf(count.getAdjustmentReason()));
                        } catch (Exception e) {
                            matched.setAdjustmentReason(variance == 0 ? StockTakeItem.AdjustmentReason.NONE : StockTakeItem.AdjustmentReason.COUNT_DISCREPANCY);
                        }
                    } else if (variance != 0) {
                        matched.setAdjustmentReason(StockTakeItem.AdjustmentReason.COUNT_DISCREPANCY);
                    } else {
                        matched.setAdjustmentReason(StockTakeItem.AdjustmentReason.NONE);
                    }

                    if (count.getNotes() != null) {
                        matched.setNotes(count.getNotes());
                    }

                    itemRepository.save(matched);
                }
            }
        }

        session.setItems(itemRepository.findBySessionSessionId(session.getSessionId()));
        return session;
    }

    @Transactional
    public StockTakeSession reconcileSession(Long sessionId, ReconcileStockTakeRequest req) {
        StockTakeSession session = getSessionById(sessionId);
        if (session.getStatus() != StockTakeSession.Status.IN_PROGRESS) {
            throw new IllegalStateException("Session is not in progress. Current status: " + session.getStatus());
        }

        // Manager verification if PIN or username provided
        if (req.getManagerPin() != null || req.getReconciledBy() != null) {
            boolean isManager = userService.verifyManager(req.getReconciledBy(), req.getManagerPin());
            if (!isManager && req.getManagerPin() != null) {
                log.warn("Stock take reconciliation override attempt by: {}", req.getReconciledBy());
            }
        }

        List<StockTakeItem> items = itemRepository.findBySessionSessionId(session.getSessionId());

        // Adjust actual physical stock
        for (StockTakeItem item : items) {
            int variance = item.getVarianceQuantity();
            if (variance != 0) {
                Product product = item.getProduct();
                List<ProductBatch> batches = productBatchRepository.findByProductProductId(product.getProductId());

                if (batches.isEmpty()) {
                    // Create an initial reconciliation batch
                    if (item.getPhysicalQuantity() > 0) {
                        ProductBatch newBatch = ProductBatch.builder()
                                .product(product)
                                .batchNumber("RECON-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                                .expiryDate(LocalDate.now().plusMonths(12))
                                .costPrice(item.getUnitCost())
                                .quantityAvailable(item.getPhysicalQuantity())
                                .supplierName("Stock Take Reconciliation")
                                .build();
                        productBatchRepository.save(newBatch);
                    }
                } else {
                    // Adjust batch stock to match physical count
                    int targetQty = item.getPhysicalQuantity();
                    if (targetQty <= 0) {
                        for (ProductBatch b : batches) {
                            b.setQuantityAvailable(0);
                            productBatchRepository.save(b);
                        }
                    } else {
                        // Distribute to first active batch
                        ProductBatch firstBatch = batches.get(0);
                        firstBatch.setQuantityAvailable(targetQty);
                        productBatchRepository.save(firstBatch);

                        for (int i = 1; i < batches.size(); i++) {
                            ProductBatch other = batches.get(i);
                            other.setQuantityAvailable(0);
                            productBatchRepository.save(other);
                        }
                    }
                }
                log.info("Reconciled stock for product {}: System was {}, Physical is {}, Variance: {}",
                        product.getBrandName(), item.getSystemQuantity(), item.getPhysicalQuantity(), variance);
            }
        }

        session.setStatus(StockTakeSession.Status.RECONCILED);
        session.setReconciledAt(LocalDateTime.now());
        session.setReconciledBy(req.getReconciledBy() != null ? req.getReconciledBy() : "Store Manager");
        if (req.getNotes() != null) {
            session.setNotes(session.getNotes() != null ? session.getNotes() + " | " + req.getNotes() : req.getNotes());
        }

        StockTakeSession saved = sessionRepository.save(session);
        log.info("Stock take session {} successfully reconciled by {}", saved.getSessionCode(), saved.getReconciledBy());
        return saved;
    }
}

