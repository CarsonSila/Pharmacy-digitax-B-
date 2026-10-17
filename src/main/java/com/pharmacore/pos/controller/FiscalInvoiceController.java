package com.pharmacore.pos.controller;

import com.pharmacore.pos.entity.FiscalInvoice;
import com.pharmacore.pos.repository.FiscalInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fiscal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FiscalInvoiceController {

    private final FiscalInvoiceRepository fiscalInvoiceRepository;

    /**
     * Lists all fiscal invoices and their KRA eTIMS status.
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<FiscalInvoice>> listFiscalInvoices(
            @RequestParam(name = "status", required = false) FiscalInvoice.FiscalStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(fiscalInvoiceRepository.findByFiscalStatus(status));
        }
        return ResponseEntity.ok(fiscalInvoiceRepository.findAll());
    }

    /**
     * Retrieves eTIMS fiscal invoice and QR code string by Order ID.
     */
    @GetMapping("/invoices/by-order/{orderId}")
    public ResponseEntity<FiscalInvoice> getInvoiceByOrder(@PathVariable("orderId") Long orderId) {
        return fiscalInvoiceRepository.findByOrderOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

