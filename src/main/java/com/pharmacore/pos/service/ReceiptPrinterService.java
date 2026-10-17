package com.pharmacore.pos.service;

import com.pharmacore.pos.entity.FiscalInvoice;
import com.pharmacore.pos.entity.Order;
import com.pharmacore.pos.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class ReceiptPrinterService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Builds a formatted 80mm thermal receipt text string.
     */
    public String buildThermalReceiptText(Order order, FiscalInvoice fiscalInvoice) {
        StringBuilder sb = new StringBuilder();

        sb.append("================================================\n");
        sb.append("              PHARMACORE PHARMACY               \n");
        sb.append("         TAX COMPLIANT RETAIL PHARMACY          \n");
        sb.append("              KRA PIN: P051982736Z              \n");
        sb.append("            NAIROBI CBD MAIN BRANCH             \n");
        sb.append("               TEL: +254 700 000 000            \n");
        sb.append("================================================\n");
        sb.append(String.format("Receipt #: %-16s Date: %s\n", 
                order.getOrderNumber(), 
                order.getCreatedAt().format(DATE_FORMATTER)));
        sb.append(String.format("Cashier:   %-16s Till: POS-01\n", 
                order.getCashier().getFullName()));
        
        if (order.getCustomer() != null && order.getCustomer().getKraPin() != null) {
            sb.append(String.format("Buyer PIN: %s (%s)\n", 
                    order.getCustomer().getKraPin(), order.getCustomer().getFullName()));
        } else if (fiscalInvoice.getBuyerPin() != null && !fiscalInvoice.getBuyerPin().isBlank()) {
            sb.append(String.format("Buyer PIN: %s\n", fiscalInvoice.getBuyerPin()));
        }

        if (order.getPrescriptionReference() != null) {
            sb.append(String.format("Rx Ref:    %s | Dr: %s\n", 
                    order.getPrescriptionReference(), 
                    order.getPrescribingDoctor() != null ? order.getPrescribingDoctor() : "N/A"));
        }
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-24s %4s %8s %9s\n", "ITEM", "QTY", "PRICE", "TOTAL"));
        sb.append("------------------------------------------------\n");

        for (OrderItem item : order.getItems()) {
            String taxCode = "[" + item.getTaxClassification().getKraTaxCode() + "]";
            String name = item.getProduct().getBrandName();
            if (name.length() > 24) name = name.substring(0, 24);

            sb.append(String.format("%-24s %4d %8.2f %9.2f %s\n",
                    name,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal(),
                    taxCode));

            // Print batch & expiry for safety & trace
            sb.append(String.format("  Batch: %s  Exp: %s (%s)\n",
                    item.getBatch().getBatchNumber(),
                    item.getBatch().getExpiryDate(),
                    item.getUnit().getName()));
        }

        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-34s KES %9.2f\n", "SUBTOTAL:", order.getSubtotalAmount()));
        if (order.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-34s KES %9.2f\n", "DISCOUNT:", order.getDiscountAmount()));
        }

        sb.append("TAX BREAKDOWN:\n");
        if (fiscalInvoice.getTotalVatAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(String.format("  [A] 16%% VAT (Net: %.2f, VAT: %.2f)\n", 
                    fiscalInvoice.getTotalTaxableAmount(), fiscalInvoice.getTotalVatAmount()));
        }
        if (fiscalInvoice.getTotalExemptAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(String.format("  [E] Exempt Pharmaceuticals:        KES %9.2f\n", 
                    fiscalInvoice.getTotalExemptAmount()));
        }
        if (fiscalInvoice.getTotalZeroRatedAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(String.format("  [B] Zero-Rated Supplies:           KES %9.2f\n", 
                    fiscalInvoice.getTotalZeroRatedAmount()));
        }

        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-30s KES %9.2f\n", "TOTAL DUE:", order.getTotalAmount()));
        sb.append(String.format("%-30s: %s\n", 
                "PAID VIA " + order.getPaymentMethod(), 
                order.getPaymentReference() != null ? order.getPaymentReference() : "CASH"));
        sb.append("================================================\n");

        // Fiscal Control Unit Information
        if (fiscalInvoice.getKraControlUnitNumber() != null) {
            sb.append(String.format("eTIMS CU Number:  %s\n", fiscalInvoice.getKraControlUnitNumber()));
            sb.append(String.format("eTIMS Invoice No: %s\n", fiscalInvoice.getKraInvoiceNumber()));
            sb.append("Verify Online:    " + fiscalInvoice.getQrCodeUrl() + "\n");
            sb.append("\n             [ KRA VERIFICATION QR CODE ]       \n\n");
        } else {
            sb.append("eTIMS STATUS: QUEUED (OFFLINE FISCALIZATION)\n");
        }

        sb.append("================================================\n");
        sb.append("   GOODS ONCE SOLD CANNOT BE ACCEPTED WITHOUT   \n");
        sb.append("          ORIGINAL RECEIPT & PACKAGING          \n");
        sb.append("          THANK YOU FOR SHOPPING WITH US!       \n");
        sb.append("================================================\n");

        return sb.toString();
    }
}

