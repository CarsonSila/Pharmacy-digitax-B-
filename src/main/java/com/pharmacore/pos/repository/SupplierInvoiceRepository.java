package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.SupplierInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long> {
    List<SupplierInvoice> findAllByOrderByCreatedAtDesc();
    Optional<SupplierInvoice> findByInvoiceNumber(String invoiceNumber);
    List<SupplierInvoice> findBySupplierSupplierIdOrderByInvoiceDateDesc(Integer supplierId);
}

