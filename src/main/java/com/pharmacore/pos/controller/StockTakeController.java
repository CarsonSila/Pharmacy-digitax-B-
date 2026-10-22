package com.pharmacore.pos.controller;

import com.pharmacore.pos.dto.CreateStockTakeRequest;
import com.pharmacore.pos.dto.ReconcileStockTakeRequest;
import com.pharmacore.pos.dto.StockTakeCountRequest;
import com.pharmacore.pos.entity.StockTakeSession;
import com.pharmacore.pos.service.StockTakeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-take")
@CrossOrigin(origins = "*")
public class StockTakeController {

    private final StockTakeService stockTakeService;

    public StockTakeController(StockTakeService stockTakeService) {
        this.stockTakeService = stockTakeService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<StockTakeSession>> getAllSessions() {
        return ResponseEntity.ok(stockTakeService.getAllSessions());
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<StockTakeSession> getSessionById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(stockTakeService.getSessionById(id));
    }

    @PostMapping("/sessions")
    public ResponseEntity<StockTakeSession> startSession(@RequestBody CreateStockTakeRequest request) {
        return ResponseEntity.ok(stockTakeService.startSession(request));
    }

    @PutMapping("/sessions/counts")
    public ResponseEntity<StockTakeSession> recordCounts(@RequestBody StockTakeCountRequest request) {
        return ResponseEntity.ok(stockTakeService.recordCounts(request));
    }

    @PostMapping("/sessions/{id}/reconcile")
    public ResponseEntity<StockTakeSession> reconcileSession(
            @PathVariable("id") Long id,
            @RequestBody ReconcileStockTakeRequest request
    ) {
        return ResponseEntity.ok(stockTakeService.reconcileSession(id, request));
    }
}

