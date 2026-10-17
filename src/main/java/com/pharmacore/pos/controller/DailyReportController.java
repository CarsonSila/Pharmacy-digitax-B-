package com.pharmacore.pos.controller;

import com.pharmacore.pos.dto.ZReportResponse;
import com.pharmacore.pos.entity.Product;
import com.pharmacore.pos.entity.ProductBatch;
import com.pharmacore.pos.repository.ProductBatchRepository;
import com.pharmacore.pos.repository.ProductRepository;
import com.pharmacore.pos.service.DailyReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class DailyReportController {

    private final DailyReportService dailyReportService;
    private final ProductBatchRepository productBatchRepository;
    private final ProductRepository productRepository;

    public DailyReportController(
            DailyReportService dailyReportService,
            ProductBatchRepository productBatchRepository,
            ProductRepository productRepository) {
        this.dailyReportService = dailyReportService;
        this.productBatchRepository = productBatchRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/z-report")
    public ResponseEntity<ZReportResponse> getZReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "Duty Cashier") String cashier) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        ZReportResponse report = dailyReportService.generateDailyReport(targetDate, "Z-REPORT", cashier);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/x-report")
    public ResponseEntity<ZReportResponse> getXReport(
            @RequestParam(required = false, defaultValue = "Duty Cashier") String cashier) {
        ZReportResponse report = dailyReportService.generateDailyReport(LocalDate.now(), "X-REPORT", cashier);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/inventory-alerts")
    public ResponseEntity<Map<String, Object>> getInventoryAlerts() {
        return ResponseEntity.ok(dailyReportService.getInventoryAlerts());
    }
}

