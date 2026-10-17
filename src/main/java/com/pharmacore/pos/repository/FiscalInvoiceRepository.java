package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.FiscalInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FiscalInvoiceRepository extends JpaRepository<FiscalInvoice, Long> {

    Optional<FiscalInvoice> findByOrderOrderId(Long orderId);

    List<FiscalInvoice> findByFiscalStatus(FiscalInvoice.FiscalStatus status);

    List<FiscalInvoice> findByFiscalStatusAndRetryCountLessThan(
            FiscalInvoice.FiscalStatus status, 
            Integer maxRetries
    );
}

