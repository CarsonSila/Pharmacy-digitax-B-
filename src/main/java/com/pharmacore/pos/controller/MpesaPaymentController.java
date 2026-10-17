package com.pharmacore.pos.controller;

import com.pharmacore.pos.dto.MpesaStkPushRequest;
import com.pharmacore.pos.dto.MpesaStkPushResponse;
import com.pharmacore.pos.service.MpesaDarajaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/mpesa")
@CrossOrigin(origins = "*")
public class MpesaPaymentController {

    private final MpesaDarajaService mpesaService;

    public MpesaPaymentController(MpesaDarajaService mpesaService) {
        this.mpesaService = mpesaService;
    }

    @PostMapping("/stk-push")
    public ResponseEntity<MpesaStkPushResponse> initiateStkPush(@RequestBody MpesaStkPushRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            MpesaStkPushResponse error = new MpesaStkPushResponse();
            error.setStatus("FAILED");
            error.setResponseDescription("Phone number is required");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            MpesaStkPushResponse error = new MpesaStkPushResponse();
            error.setStatus("FAILED");
            error.setResponseDescription("Amount must be greater than zero");
            return ResponseEntity.badRequest().body(error);
        }

        MpesaStkPushResponse response = mpesaService.initiateStkPush(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{checkoutRequestId}")
    public ResponseEntity<MpesaStkPushResponse> checkStatus(@PathVariable String checkoutRequestId) {
        MpesaStkPushResponse status = mpesaService.getStatus(checkoutRequestId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> mpesaCallback(@RequestBody Map<String, Object> callbackData) {
        mpesaService.handleCallback(callbackData);
        return ResponseEntity.ok(Map.of("ResultCode", "0", "ResultDesc", "Accepted"));
    }
}

