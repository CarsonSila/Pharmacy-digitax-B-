package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.ZReportResponse;
import com.pharmacore.pos.entity.*;
import com.pharmacore.pos.repository.FiscalInvoiceRepository;
import com.pharmacore.pos.repository.OrderRepository;
import com.pharmacore.pos.repository.ProductBatchRepository;
import com.pharmacore.pos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DailyReportService {

    private final OrderRepository orderRepository;
    private final FiscalInvoiceRepository fiscalInvoiceRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductRepository productRepository;

    @Value("${digitax.branch-code:MAIN_BRANCH}")
    private String branchCode;

    public DailyReportService(
            OrderRepository orderRepository,
            FiscalInvoiceRepository fiscalInvoiceRepository,
            ProductBatchRepository productBatchRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.fiscalInvoiceRepository = fiscalInvoiceRepository;
        this.productBatchRepository = productBatchRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ZReportResponse generateDailyReport(LocalDate targetDate, String reportType, String cashierName) {
        if (targetDate == null) {
            targetDate = LocalDate.now();
        }
        if (reportType == null || reportType.trim().isEmpty()) {
            reportType = "Z-REPORT";
        }
        if (cashierName == null || cashierName.trim().isEmpty()) {
            cashierName = "Duty Cashier / System";
        }

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        ZReportResponse report = new ZReportResponse();
        report.setReportType(reportType.toUpperCase());
        report.setReportDate(targetDate);
        report.setGeneratedAt(LocalDateTime.now());
        report.setPharmacyName("PHARMACORE RETAIL PHARMACY");
        report.setKraPin("P051234567Z");
        report.setBranchCode(branchCode);
        report.setCashierName(cashierName);

        int totalOrders = orders.size();
        int totalItemsSold = 0;
        BigDecimal grossSales = BigDecimal.ZERO;
        BigDecimal netSales = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        BigDecimal vat16Gross = BigDecimal.ZERO;
        BigDecimal vat16Net = BigDecimal.ZERO;
        BigDecimal vat16Tax = BigDecimal.ZERO;
        BigDecimal zeroRatedSales = BigDecimal.ZERO;
        BigDecimal exemptSales = BigDecimal.ZERO;

        BigDecimal cashTotal = BigDecimal.ZERO;
        int cashCount = 0;
        BigDecimal mpesaTotal = BigDecimal.ZERO;
        int mpesaCount = 0;
        BigDecimal cardTotal = BigDecimal.ZERO;
        int cardCount = 0;
        BigDecimal insuranceTotal = BigDecimal.ZERO;
        int insuranceCount = 0;

        for (Order order : orders) {
            grossSales = grossSales.add(order.getTotalAmount());
            netSales = netSales.add(order.getSubtotalAmount().subtract(order.getDiscountAmount()));
            totalDiscount = totalDiscount.add(order.getDiscountAmount());
            totalTax = totalTax.add(order.getTaxAmount());

            // Payment tender classification
            if (order.getPaymentMethod() != null) {
                switch (order.getPaymentMethod()) {
                    case CASH:
                        cashTotal = cashTotal.add(order.getTotalAmount());
                        cashCount++;
                        break;
                    case MPESA:
                        mpesaTotal = mpesaTotal.add(order.getTotalAmount());
                        mpesaCount++;
                        break;
                    case CARD:
                        cardTotal = cardTotal.add(order.getTotalAmount());
                        cardCount++;
                        break;
                    case INSURANCE:
                        insuranceTotal = insuranceTotal.add(order.getTotalAmount());
                        insuranceCount++;
                        break;
                    default:
                        cashTotal = cashTotal.add(order.getTotalAmount());
                        cashCount++;
                }
            }

            // Line items tax classification
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    totalItemsSold += item.getQuantity();
                    String code = item.getTaxClassification() != null ? item.getTaxClassification().getClassCode() : "EXEMPT";
                    if ("VAT_16".equalsIgnoreCase(code)) {
                        vat16Gross = vat16Gross.add(item.getLineTotal());
                        vat16Tax = vat16Tax.add(item.getTaxAmount());
                        vat16Net = vat16Net.add(item.getLineTotal().subtract(item.getTaxAmount()));
                    } else if ("ZERO_RATED".equalsIgnoreCase(code)) {
                        zeroRatedSales = zeroRatedSales.add(item.getLineTotal());
                    } else {
                        // EXEMPT
                        exemptSales = exemptSales.add(item.getLineTotal());
                    }
                }
            }
        }

        report.setTotalOrders(totalOrders);
        report.setTotalItemsSold(totalItemsSold);
        report.setGrossSales(grossSales.setScale(2, RoundingMode.HALF_UP));
        report.setNetSales(netSales.setScale(2, RoundingMode.HALF_UP));
        report.setTotalDiscount(totalDiscount.setScale(2, RoundingMode.HALF_UP));
        report.setTotalTax(totalTax.setScale(2, RoundingMode.HALF_UP));

        report.setVat16Gross(vat16Gross.setScale(2, RoundingMode.HALF_UP));
        report.setVat16Net(vat16Net.setScale(2, RoundingMode.HALF_UP));
        report.setVat16Tax(vat16Tax.setScale(2, RoundingMode.HALF_UP));
        report.setZeroRatedSales(zeroRatedSales.setScale(2, RoundingMode.HALF_UP));
        report.setExemptSales(exemptSales.setScale(2, RoundingMode.HALF_UP));

        report.setCashTotal(cashTotal.setScale(2, RoundingMode.HALF_UP));
        report.setCashCount(cashCount);
        report.setMpesaTotal(mpesaTotal.setScale(2, RoundingMode.HALF_UP));
        report.setMpesaCount(mpesaCount);
        report.setCardTotal(cardTotal.setScale(2, RoundingMode.HALF_UP));
        report.setCardCount(cardCount);
        report.setInsuranceTotal(insuranceTotal.setScale(2, RoundingMode.HALF_UP));
        report.setInsuranceCount(insuranceCount);

        // Fiscal status audits
        int completed = 0;
        int queued = 0;
        int failed = 0;
        for (Order order : orders) {
            var invoiceOpt = fiscalInvoiceRepository.findByOrderOrderId(order.getOrderId());
            if (invoiceOpt.isPresent()) {
                FiscalInvoice inv = invoiceOpt.get();
                if (inv.getFiscalStatus() == FiscalInvoice.FiscalStatus.COMPLETED) {
                    completed++;
                } else if (inv.getFiscalStatus() == FiscalInvoice.FiscalStatus.QUEUED ||
                           inv.getFiscalStatus() == FiscalInvoice.FiscalStatus.PROCESSING) {
                    queued++;
                } else if (inv.getFiscalStatus() == FiscalInvoice.FiscalStatus.FAILED) {
                    failed++;
                }
            }
        }
        report.setFiscalCompletedCount(completed);
        report.setFiscalQueuedCount(queued);
        report.setFiscalFailedCount(failed);

        // Inventory health
        List<ProductBatch> expiredBatches = productBatchRepository.findByExpiryDateBefore(LocalDate.now());
        List<ProductBatch> expiringSoon = productBatchRepository.findByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(90));
        report.setExpiredBatchesCount(expiredBatches.size());
        report.setExpiringSoonBatchesCount(expiringSoon.size());

        // Low stock count
        int lowStock = 0;
        List<Product> allProducts = productRepository.findAll();
        for (Product p : allProducts) {
            int currentStock = p.getBatches() != null ?
                    p.getBatches().stream().mapToInt(ProductBatch::getQuantityAvailable).sum() : 0;
            if (currentStock <= (p.getReorderLevel() != null ? p.getReorderLevel() : 20)) {
                lowStock++;
            }
        }
        report.setLowStockProductsCount(lowStock);

        // Generate ESC/POS 80mm thermal text slip
        report.setThermalReportText(buildThermalText(report));

        return report;
    }

    private String buildThermalText(ZReportResponse r) {
        StringBuilder sb = new StringBuilder();
        String line = "------------------------------------------------";
        String doubleLine = "================================================";

        sb.append(doubleLine).append("\n");
        sb.append(center(r.getPharmacyName())).append("\n");
        sb.append(center("DAILY FISCAL " + r.getReportType())).append("\n");
        sb.append(center("KRA PIN: " + r.getKraPin() + " | BRANCH: " + r.getBranchCode())).append("\n");
        sb.append(doubleLine).append("\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        sb.append(String.format("Report Date   : %s\n", r.getReportDate()));
        sb.append(String.format("Generated At  : %s\n", r.getGeneratedAt().format(dtf)));
        sb.append(String.format("Cashier/User  : %s\n", r.getCashierName()));
        sb.append(line).append("\n");

        sb.append(center("SALES SUMMARY")).append("\n");
        sb.append(String.format("%-28s %18d\n", "Total Completed Orders:", r.getTotalOrders()));
        sb.append(String.format("%-28s %18d\n", "Total Items Sold:", r.getTotalItemsSold()));
        sb.append(String.format("%-28s %18s\n", "Net Sales (Excl. Tax):", "KES " + r.getNetSales()));
        sb.append(String.format("%-28s %18s\n", "Total Discount Given:", "KES " + r.getTotalDiscount()));
        sb.append(String.format("%-28s %18s\n", "Total VAT Collected:", "KES " + r.getTotalTax()));
        sb.append(String.format("%-28s %18s\n", "GROSS SALES:", "KES " + r.getGrossSales()));
        sb.append(line).append("\n");

        sb.append(center("KRA eTIMS TAX ANALYSIS")).append("\n");
        sb.append(String.format("%-18s %14s %14s\n", "Tax Band", "Taxable Base", "Tax Amount"));
        sb.append(String.format("%-18s %14s %14s\n", "A - 16% Standard", r.getVat16Net(), r.getVat16Tax()));
        sb.append(String.format("%-18s %14s %14s\n", "B - 0% Zero-Rate", r.getZeroRatedSales(), "0.00"));
        sb.append(String.format("%-18s %14s %14s\n", "E - Exempt Drugs", r.getExemptSales(), "0.00"));
        sb.append(line).append("\n");

        sb.append(center("TENDER SUMMARY")).append("\n");
        sb.append(String.format("%-20s %6d %20s\n", "Cash Tendered:", r.getCashCount(), "KES " + r.getCashTotal()));
        sb.append(String.format("%-20s %6d %20s\n", "M-Pesa Express:", r.getMpesaCount(), "KES " + r.getMpesaTotal()));
        sb.append(String.format("%-20s %6d %20s\n", "Debit/Credit Card:", r.getCardCount(), "KES " + r.getCardTotal()));
        sb.append(String.format("%-20s %6d %20s\n", "Insurance Claims:", r.getInsuranceCount(), "KES " + r.getInsuranceTotal()));
        sb.append(line).append("\n");

        sb.append(center("DIGITAX eTIMS TRANSMISSION")).append("\n");
        sb.append(String.format("%-32s %14d\n", "Invoices Synced (COMPLETED):", r.getFiscalCompletedCount()));
        sb.append(String.format("%-32s %14d\n", "Offline Queued (PENDING):", r.getFiscalQueuedCount()));
        sb.append(String.format("%-32s %14d\n", "Failed / Attention Required:", r.getFiscalFailedCount()));
        sb.append(line).append("\n");

        sb.append(center("INVENTORY & BATCH ALERTS")).append("\n");
        sb.append(String.format("%-32s %14d\n", "Expired Batches:", r.getExpiredBatchesCount()));
        sb.append(String.format("%-32s %14d\n", "Batches Expiring in <90d:", r.getExpiringSoonBatchesCount()));
        sb.append(String.format("%-32s %14d\n", "Low Stock Products (<20):", r.getLowStockProductsCount()));
        sb.append(doubleLine).append("\n");
        sb.append(center("*** END OF FISCAL " + r.getReportType() + " ***")).append("\n");
        sb.append(doubleLine).append("\n");

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getInventoryAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate in90Days = today.plusDays(90);

        List<ProductBatch> expired = productBatchRepository.findByExpiryDateBefore(today);
        List<ProductBatch> expiringSoon = productBatchRepository.findByExpiryDateBetween(today, in90Days);
        List<Product> allProducts = productRepository.findAll();

        List<java.util.Map<String, Object>> lowStockItems = new java.util.ArrayList<>();
        for (Product p : allProducts) {
            int currentStock = 0;
            if (p.getBatches() != null) {
                for (ProductBatch b : p.getBatches()) {
                    if (b.getQuantityAvailable() != null) {
                        currentStock += b.getQuantityAvailable();
                    }
                }
            }
            int threshold = (p.getReorderLevel() != null) ? p.getReorderLevel() : 20;
            if (currentStock <= threshold) {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("productId", p.getProductId());
                item.put("brandName", p.getBrandName());
                item.put("genericName", p.getGenericName());
                item.put("sku", p.getSku());
                item.put("currentStock", currentStock);
                item.put("reorderLevel", threshold);
                lowStockItems.add(item);
            }
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("expiredBatches", expired);
        response.put("expiringSoonBatches", expiringSoon);
        response.put("lowStockProducts", lowStockItems);
        response.put("expiredCount", expired.size());
        response.put("expiringSoonCount", expiringSoon.size());
        response.put("lowStockCount", lowStockItems.size());
        return response;
    }

    private String center(String text) {
        int width = 48;
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }
}

