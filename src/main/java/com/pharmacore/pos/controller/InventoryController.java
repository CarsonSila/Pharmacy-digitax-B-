package com.pharmacore.pos.controller;

import com.pharmacore.pos.dto.CreateBatchRequest;
import com.pharmacore.pos.dto.CreateProductRequest;
import com.pharmacore.pos.entity.Product;
import com.pharmacore.pos.entity.ProductBatch;
import com.pharmacore.pos.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request) {
        if (request.getBrandName() == null || request.getBrandName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getSellingPrice() == null || request.getSellingPrice().signum() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Product created = inventoryService.createProduct(request);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/batches")
    public ResponseEntity<ProductBatch> addBatch(@RequestBody CreateBatchRequest request) {
        if (request.getProductId() == null || request.getBatchNumber() == null || request.getExpiryDate() == null) {
            return ResponseEntity.badRequest().build();
        }
        ProductBatch created = inventoryService.addBatch(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        return ResponseEntity.ok(inventoryService.getInventoryMetadata());
    }

    @PutMapping("/products/{productId}/price")
    public ResponseEntity<Product> updatePrice(
            @PathVariable("productId") Integer productId,
            @RequestBody com.pharmacore.pos.dto.UpdatePriceRequest request
    ) {
        if (request.getSellingPrice() == null || request.getSellingPrice().signum() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Product updated = inventoryService.updateProductPrice(productId, request);
        return ResponseEntity.ok(updated);
    }
}

