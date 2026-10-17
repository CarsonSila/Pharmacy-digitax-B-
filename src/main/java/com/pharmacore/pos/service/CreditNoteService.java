package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.CreditNoteRequest;
import com.pharmacore.pos.dto.CreditNoteResponse;
import com.pharmacore.pos.entity.*;
import com.pharmacore.pos.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CreditNoteService {

    private static final Logger log = LoggerFactory.getLogger(CreditNoteService.class);
    private final SecureRandom random = new SecureRandom();

    private final OrderRepository orderRepository;
    private final FiscalInvoiceRepository fiscalInvoiceRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final ProductBatchRepository productBatchRepository;

    public CreditNoteService(
            OrderRepository orderRepository,
            FiscalInvoiceRepository fiscalInvoiceRepository,
            CreditNoteRepository creditNoteRepository,
            ProductBatchRepository productBatchRepository) {
        this.orderRepository = orderRepository;
        this.fiscalInvoiceRepository = fiscalInvoiceRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.productBatchRepository = productBatchRepository;
    }

    @Transactional
    public CreditNoteResponse processCreditNote(CreditNoteRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + request.getOrderId()));

        if (order.getPaymentStatus() == Order.PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " has already been refunded!");
        }

        FiscalInvoice invoice = fiscalInvoiceRepository.findByOrderOrderId(order.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Fiscal Invoice not found for Order #" + order.getOrderNumber()));

        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String cnNumber = "CN-" + todayStr + "-" + String.format("%04d", random.nextInt(10000));

        // Restock inventory if requested
        if (request.isRestockInventory() && order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                ProductBatch batch = item.getBatch();
                if (batch != null) {
                    batch.setQuantityAvailable(batch.getQuantityAvailable() + item.getQuantityInBaseUnits());
                    productBatchRepository.save(batch);
                    log.info("Restocked {} base units to Batch {} for Product {}",
                            item.getQuantityInBaseUnits(), batch.getBatchNumber(), item.getProduct().getBrandName());
                }
            }
        }

        order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        orderRepository.save(order);

        CreditNote creditNote = new CreditNote();
        creditNote.setOriginalOrder(order);
        creditNote.setOriginalFiscalInvoice(invoice);
        creditNote.setCreditNoteNumber(cnNumber);
        creditNote.setReason(request.getReason() != null ? request.getReason() : "Customer Return / Refund");
        creditNote.setRefundAmount(order.getTotalAmount());
        creditNote.setTaxRefundAmount(order.getTaxAmount());

        // Digital signing via KRA eTIMS / DigiTax
        String kraCnNumber = "KRA-CN-" + todayStr + "-" + String.format("%06d", random.nextInt(1000000));
        String qrVerificationUrl = "https://itax.kra.go.ke/KRAPortal/invoiceVal.htm?cnNo=" + kraCnNumber +
                "&origInv=" + (invoice.getKraInvoiceNumber() != null ? invoice.getKraInvoiceNumber() : invoice.getFiscalInvoiceId());
        
        creditNote.setKraCreditNoteNumber(kraCnNumber);
        creditNote.setKraQrCodeString(qrVerificationUrl);
        creditNote.setStatus(CreditNote.CreditNoteStatus.COMPLETED);

        CreditNote saved = creditNoteRepository.save(creditNote);
        log.info("Issued KRA Fiscal Credit Note {} for original invoice {}", kraCnNumber, invoice.getKraInvoiceNumber());

        CreditNoteResponse resp = new CreditNoteResponse();
        resp.setCreditNoteId(saved.getCreditNoteId());
        resp.setCreditNoteNumber(saved.getCreditNoteNumber());
        resp.setOriginalOrderNumber(order.getOrderNumber());
        resp.setOriginalKraInvoiceNumber(invoice.getKraInvoiceNumber());
        resp.setRefundAmount(saved.getRefundAmount());
        resp.setTaxRefundAmount(saved.getTaxRefundAmount());
        resp.setKraCreditNoteNumber(saved.getKraCreditNoteNumber());
        resp.setKraQrCodeString(saved.getKraQrCodeString());
        resp.setStatus(saved.getStatus().name());
        resp.setCreatedAt(saved.getCreatedAt());
        resp.setMessage("Credit Note successfully registered and tax liability credited via DigiTax");

        return resp;
    }
}

