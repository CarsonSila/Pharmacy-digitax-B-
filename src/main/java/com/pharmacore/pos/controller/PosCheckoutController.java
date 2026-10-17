package com.pharmacore.pos.controller;

import com.pharmacore.pos.dto.CheckoutRequest;
import com.pharmacore.pos.dto.CheckoutResponse;
import com.pharmacore.pos.entity.Product;
import com.pharmacore.pos.entity.ProductBatch;
import com.pharmacore.pos.repository.ProductBatchRepository;
import com.pharmacore.pos.repository.ProductRepository;
import com.pharmacore.pos.service.PosCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PosCheckoutController {

    private final PosCheckoutService posCheckoutService;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;

    /**
     * Main POS Checkout Endpoint.
     * Takes cart items, allocates FEFO batches, saves order, and submits to DigiTax.
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = posCheckoutService.processCheckout(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search products by barcode, SKU, or brand name (e.g. for cashier scanner).
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(name = "q", required = false, defaultValue = "") String query
    ) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(productRepository.findAll());
        }
        return ResponseEntity.ok(productRepository.searchActiveProducts(query.trim()));
    }

    /**
     * Look up available batches for a product ordered by earliest expiry (FEFO).
     */
    @GetMapping("/products/{productId}/batches")
    public ResponseEntity<List<ProductBatch>> getProductBatches(@PathVariable("productId") Integer productId) {
        return ResponseEntity.ok(productBatchRepository.findAvailableBatchesFefo(productId, LocalDate.now()));
    }
}

