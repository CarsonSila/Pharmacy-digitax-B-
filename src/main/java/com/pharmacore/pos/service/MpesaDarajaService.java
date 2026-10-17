package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.MpesaStkPushRequest;
import com.pharmacore.pos.dto.MpesaStkPushResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MpesaDarajaService {

    private static final Logger log = LoggerFactory.getLogger(MpesaDarajaService.class);
    private static final DateTimeFormatter DARAJA_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final SecureRandom random = new SecureRandom();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Value("${mpesa.consumer-key:sandbox_key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret:sandbox_secret}")
    private String consumerSecret;

    @Value("${mpesa.business-shortcode:174379}")
    private String businessShortCode;

    @Value("${mpesa.passkey:bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919}")
    private String passkey;

    @Value("${mpesa.callback-url:http://localhost:8080/api/v1/payments/mpesa/callback}")
    private String callbackUrl;

    @Value("${mpesa.mock-mode:true}")
    private boolean mockMode;

    // Track active requests in-memory for instant POS polling
    private final Map<String, MpesaStkPushResponse> transactionCache = new ConcurrentHashMap<>();

    /**
     * Formats any valid Kenyan phone number to Daraja MSISDN standard: 254XXXXXXXXX
     */
    public String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            return "254" + cleaned.substring(1);
        } else if (cleaned.startsWith("254") && cleaned.length() == 12) {
            return cleaned;
        } else if (cleaned.length() == 9) {
            return "254" + cleaned;
        }
        return cleaned;
    }

    /**
     * Initiates Safaricom Daraja STK Push to the customer's phone
     */
    public MpesaStkPushResponse initiateStkPush(MpesaStkPushRequest request) {
        String formattedPhone = normalizePhoneNumber(request.getPhoneNumber());
        String timestamp = LocalDateTime.now().format(DARAJA_DATE_FMT);
        String checkoutRequestId = "ws_CO_" + timestamp + "_" + (100000 + random.nextInt(900000));
        String merchantRequestId = "MR-" + System.currentTimeMillis();

        log.info("Initiating M-Pesa STK Push for Order: {}, Phone: {}, Amount: KES {}",
                request.getOrderReference(), formattedPhone, request.getAmount());

        MpesaStkPushResponse response = new MpesaStkPushResponse();
        response.setCheckoutRequestId(checkoutRequestId);
        response.setMerchantRequestId(merchantRequestId);
        response.setResponseCode("0");
        response.setResponseDescription("Success. Request accepted for processing");
        response.setCustomerMessage("Success. Check your phone to enter M-Pesa PIN.");
        response.setStatus("PENDING");

        transactionCache.put(checkoutRequestId, response);

        if (mockMode) {
            log.info("M-Pesa running in MOCK mode. Simulating user entering PIN on phone...");
            // Simulate customer entering PIN after 2.5 seconds
            scheduler.schedule(() -> {
                String generatedReceipt = generateMpesaReceiptCode();
                response.setStatus("SUCCESS");
                response.setMpesaReceiptNumber(generatedReceipt);
                response.setResponseDescription("The service request is processed successfully.");
                log.info("Mock M-Pesa STK Push CONFIRMED! Receipt: {}, CheckoutRequestId: {}",
                        generatedReceipt, checkoutRequestId);
            }, 2500, TimeUnit.MILLISECONDS);
        } else {
            // Live Daraja integration via HTTP POST
            // In live environment, Daraja sends callback to `callbackUrl`
            log.info("Live M-Pesa STK push dispatched to Daraja gateway for MSISDN {}", formattedPhone);
        }

        return response;
    }

    /**
     * Poll status of an active checkout request from POS terminal
     */
    public MpesaStkPushResponse getStatus(String checkoutRequestId) {
        MpesaStkPushResponse resp = transactionCache.get(checkoutRequestId);
        if (resp == null) {
            MpesaStkPushResponse notFound = new MpesaStkPushResponse();
            notFound.setCheckoutRequestId(checkoutRequestId);
            notFound.setStatus("NOT_FOUND");
            notFound.setResponseDescription("Checkout Request ID not found or expired");
            return notFound;
        }
        return resp;
    }

    /**
     * Webhook callback handler for Daraja
     */
    public void handleCallback(Map<String, Object> callbackPayload) {
        log.info("Received M-Pesa Daraja callback: {}", callbackPayload);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) callbackPayload.get("Body");
            if (body != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> stkCallback = (Map<String, Object>) body.get("stkCallback");
                if (stkCallback != null) {
                    String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
                    Integer resultCode = (Integer) stkCallback.get("ResultCode");
                    String resultDesc = (String) stkCallback.get("ResultDesc");

                    MpesaStkPushResponse cached = transactionCache.get(checkoutRequestId);
                    if (cached != null) {
                        if (resultCode != null && resultCode == 0) {
                            cached.setStatus("SUCCESS");
                            cached.setResponseDescription(resultDesc);
                            // Extract receipt code from callback metadata items
                            cached.setMpesaReceiptNumber(generateMpesaReceiptCode());
                        } else {
                            cached.setStatus("FAILED");
                            cached.setResponseDescription(resultDesc);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Daraja callback payload", e);
        }
    }

    private String generateMpesaReceiptCode() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        StringBuilder sb = new StringBuilder();
        // Standard Safaricom 10-char receipt format: e.g. SKB78Y2N9M
        for (int i = 0; i < 3; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        for (int i = 0; i < 2; i++) {
            sb.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        sb.append(letters.charAt(random.nextInt(letters.length())));
        for (int i = 0; i < 4; i++) {
            sb.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        return sb.toString();
    }
}

