package com.pharmacore.pos.controller;

import com.pharmacore.pos.dto.CreateSupplierInvoiceRequest;
import com.pharmacore.pos.dto.CreateSupplierRequest;
import com.pharmacore.pos.entity.Supplier;
import com.pharmacore.pos.entity.SupplierInvoice;
import com.pharmacore.pos.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody CreateSupplierRequest request) {
        try {
            Supplier created = supplierService.createSupplier(request);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<SupplierInvoice>> getAllInvoices() {
        return ResponseEntity.ok(supplierService.getAllInvoices());
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<SupplierInvoice> getInvoiceById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(supplierService.getInvoiceById(id));
    }

    @PostMapping("/invoices")
    public ResponseEntity<SupplierInvoice> createInvoice(@RequestBody CreateSupplierInvoiceRequest request) {
        try {
            SupplierInvoice created = supplierService.createSupplierInvoice(request);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

