package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.DigiTaxInvoicePayload;
import com.pharmacore.pos.dto.DigiTaxResponse;
import com.pharmacore.pos.entity.FiscalInvoice;
import com.pharmacore.pos.entity.FiscalInvoice.FiscalStatus;
import com.pharmacore.pos.entity.Order;
import com.pharmacore.pos.entity.OrderItem;
import com.pharmacore.pos.repository.FiscalInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FiscalSyncWorker {

    private final FiscalInvoiceRepository fiscalInvoiceRepository;
    private final DigiTaxGatewayService digiTaxGatewayService;

    private static final int MAX_RETRIES = 5;

    /**
     * Runs every 30 seconds to retry un-fiscalized invoices.
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retryQueuedInvoices() {
        List<FiscalInvoice> pendingInvoices = fiscalInvoiceRepository
                .findByFiscalStatusAndRetryCountLessThan(FiscalStatus.QUEUED, MAX_RETRIES);

        if (pendingInvoices.isEmpty()) {
            return;
        }

        log.info("FiscalSyncWorker: Found {} queued invoices to sync with DigiTax...", pendingInvoices.size());

        for (FiscalInvoice invoice : pendingInvoices) {
            try {
                Order order = invoice.getOrder();
                invoice.setFiscalStatus(FiscalStatus.PROCESSING);
                invoice.setRetryCount(invoice.getRetryCount() + 1);

                List<DigiTaxInvoicePayload.ItemInfo> items = order.getItems().stream()
                        .map(i -> DigiTaxInvoicePayload.ItemInfo.builder()
                                .code(i.getProduct().getSku())
                                .name(i.getProduct().getBrandName())
                                .hsCode(i.getProduct().getHsCode())
                                .taxType(i.getTaxClassification().getClassCode())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .discount(i.getDiscount())
                                .build())
                        .toList();

                DigiTaxInvoicePayload payload = DigiTaxInvoicePayload.builder()
                        .branchCode(invoice.getBranchCode())
                        .cashier(invoice.getCashierName())
                        .buyer(DigiTaxInvoicePayload.BuyerInfo.builder()
                                .pin(invoice.getBuyerPin())
                                .name(invoice.getBuyerName())
                                .phone(invoice.getBuyerPhone())
                                .build())
                        .items(items)
                        .payment(DigiTaxInvoicePayload.PaymentInfo.builder()
                                .method(order.getPaymentMethod().name())
                                .reference(order.getPaymentReference())
                                .amountPaid(invoice.getGrandTotal())
                                .build())
                        .build();

                DigiTaxResponse response = digiTaxGatewayService.fiscalizeInvoice(payload);

                if ("SUCCESS".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                    invoice.setFiscalStatus(FiscalStatus.COMPLETED);
                    invoice.setKraControlUnitNumber(response.getData().getKraControlUnitNumber());
                    invoice.setKraInvoiceNumber(response.getData().getKraInvoiceNumber());
                    invoice.setQrCodeString(response.getData().getQrCodeString());
                    invoice.setQrCodeUrl(response.getData().getQrCodeUrl());
                    invoice.setFiscalizedAt(LocalDateTime.now());
                    invoice.setLastErrorMessage(null);
                    log.info("FiscalSyncWorker: Invoice #{} successfully fiscalized: {}",
                            order.getOrderNumber(), invoice.getKraInvoiceNumber());
                } else {
                    invoice.setFiscalStatus(FiscalStatus.QUEUED);
                    invoice.setLastErrorMessage(response.getMessage());
                }
            } catch (Exception ex) {
                invoice.setFiscalStatus(FiscalStatus.QUEUED);
                invoice.setLastErrorMessage("Exception during sync: " + ex.getMessage());
                log.error("FiscalSyncWorker: Error syncing invoice {}: {}", invoice.getFiscalInvoiceId(), ex.getMessage());
            }

            fiscalInvoiceRepository.save(invoice);
        }
    }
}

