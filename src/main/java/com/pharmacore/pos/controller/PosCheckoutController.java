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
    private final com.pharmacore.pos.service.QrCodeGeneratorService qrCodeGeneratorService;
    private final com.pharmacore.pos.repository.FiscalInvoiceRepository fiscalInvoiceRepository;
    private final com.pharmacore.pos.repository.OrderRepository orderRepository;
    private final com.pharmacore.pos.service.ReceiptPrinterService receiptPrinterService;

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

    /**
     * Generates a PNG verification QR code for a fiscalized order.
     */
    @GetMapping(value = "/orders/{orderId}/qr-image", produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getOrderQrImage(
            @PathVariable("orderId") Long orderId,
            @RequestParam(name = "size", defaultValue = "250") int size
    ) {
        return fiscalInvoiceRepository.findByOrderOrderId(orderId)
                .map(inv -> {
                    String target = inv.getQrCodeUrl() != null ? inv.getQrCodeUrl() : inv.getQrCodeString();
                    if (target == null) target = "KRA-PENDING";
                    byte[] png = qrCodeGeneratorService.generateQrCodePng(target, size, size);
                    return ResponseEntity.ok(png);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves the formatted 80mm thermal receipt text for reprint or serial printing.
     */
    @GetMapping(value = "/orders/{orderId}/receipt", produces = org.springframework.http.MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getOrderReceiptText(@PathVariable("orderId") Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    var inv = fiscalInvoiceRepository.findByOrderOrderId(orderId).orElse(null);
                    return ResponseEntity.ok(receiptPrinterService.buildThermalReceiptText(order, inv));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

