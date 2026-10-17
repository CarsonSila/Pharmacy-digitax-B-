package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.DigiTaxInvoicePayload;
import com.pharmacore.pos.dto.DigiTaxResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class DigiTaxGatewayService {

    private final RestClient restClient;
    private final boolean mockMode;
    private final String branchCode;

    public DigiTaxGatewayService(
            @Value("${digitax.base-url:https://api.digitax.tech}") String baseUrl,
            @Value("${digitax.api-key:sandbox_key}") String apiKey,
            @Value("${digitax.branch-code:MAIN_BRANCH}") String branchCode,
            @Value("${digitax.mock-mode:true}") boolean mockMode
    ) {
        this.mockMode = mockMode;
        this.branchCode = branchCode;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Submits invoice payload to DigiTax for KRA eTIMS signing.
     */
    public DigiTaxResponse fiscalizeInvoice(DigiTaxInvoicePayload payload) {
        if (mockMode) {
            log.info("DigiTax mock-mode enabled. Generating simulated KRA eTIMS fiscal response...");
            return generateMockKRADigiTaxResponse(payload);
        }

        try {
            log.info("Sending invoice to DigiTax API: {}", payload);
            return restClient.post()
                    .uri("/v1/invoices")
                    .body(payload)
                    .retrieve()
                    .body(DigiTaxResponse.class);
        } catch (Exception ex) {
            log.error("Failed to connect to DigiTax API: {}. Queuing for offline sync.", ex.getMessage());
            return DigiTaxResponse.builder()
                    .status("QUEUED")
                    .message("Network timeout or DigiTax error: " + ex.getMessage())
                    .build();
        }
    }

    private DigiTaxResponse generateMockKRADigiTaxResponse(DigiTaxInvoicePayload payload) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String cuNumber = "KRADIGITAX" + timestamp.substring(2, 8);
        String invoiceNumber = "KRA-" + timestamp.substring(0, 8) + "-" + shortId;

        BigDecimal totalGross = payload.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Verification QR code format standard for eTIMS
        String qrVerificationUrl = "https://etims.kra.go.ke/verify?cu=" + cuNumber + "&inv=" + invoiceNumber;
        String qrCodeString = String.format("KRA|%s|%s|%s|%s|%s",
                cuNumber, invoiceNumber, totalGross.toPlainString(), timestamp, UUID.randomUUID().toString().replace("-", ""));

        return DigiTaxResponse.builder()
                .status("SUCCESS")
                .message("Invoice successfully signed by DigiTax / KRA eTIMS")
                .data(DigiTaxResponse.DigiTaxData.builder()
                        .internalInvoiceNumber("INV-" + timestamp.substring(0, 8) + "-" + shortId)
                        .kraControlUnitNumber(cuNumber)
                        .kraInvoiceNumber(invoiceNumber)
                        .qrCodeUrl(qrVerificationUrl)
                        .qrCodeString(qrCodeString)
                        .taxBreakdown(DigiTaxResponse.TaxBreakdown.builder()
                                .vatableAmount(BigDecimal.ZERO)
                                .vatAmount(BigDecimal.ZERO)
                                .exemptAmount(totalGross)
                                .zeroRatedAmount(BigDecimal.ZERO)
                                .build())
                        .build())
                .build();
    }
}

