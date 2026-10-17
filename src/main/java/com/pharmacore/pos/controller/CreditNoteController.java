package com.pharmacore.pos.controller;

import com.pharmacore.pos.dto.CreditNoteRequest;
import com.pharmacore.pos.dto.CreditNoteResponse;
import com.pharmacore.pos.entity.CreditNote;
import com.pharmacore.pos.repository.CreditNoteRepository;
import com.pharmacore.pos.service.CreditNoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credit-notes")
@CrossOrigin(origins = "*")
public class CreditNoteController {

    private final CreditNoteService creditNoteService;
    private final CreditNoteRepository creditNoteRepository;

    public CreditNoteController(CreditNoteService creditNoteService, CreditNoteRepository creditNoteRepository) {
        this.creditNoteService = creditNoteService;
        this.creditNoteRepository = creditNoteRepository;
    }

    @PostMapping
    public ResponseEntity<CreditNoteResponse> createCreditNote(@RequestBody CreditNoteRequest request) {
        CreditNoteResponse response = creditNoteService.processCreditNote(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditNote> getCreditNote(@PathVariable Long id) {
        return creditNoteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<CreditNote>> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(creditNoteRepository.findByOriginalOrderOrderId(orderId));
    }
}

