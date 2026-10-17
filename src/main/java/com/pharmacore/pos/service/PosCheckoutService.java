package com.pharmacore.pos.service;

import com.pharmacore.pos.dto.*;
import com.pharmacore.pos.entity.*;
import com.pharmacore.pos.entity.FiscalInvoice.FiscalStatus;
import com.pharmacore.pos.entity.Order.PaymentMethod;
import com.pharmacore.pos.entity.Order.PaymentStatus;
import com.pharmacore.pos.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosCheckoutService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final FiscalInvoiceRepository fiscalInvoiceRepository;
    private final InventoryFefoService inventoryFefoService;
    private final TaxEngineService taxEngineService;
    private final DigiTaxGatewayService digiTaxGatewayService;
    private final ReceiptPrinterService receiptPrinterService;

    @Transactional
    public CheckoutResponse processCheckout(CheckoutRequest request) {
        User cashier = userRepository.findById(request.getCashierId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Cashier ID: " + request.getCashierId()));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId()).orElse(null);
        }

        // Generate clean unique human-readable Order Number: ORD-YYYYMMDD-XXXX
        String orderNumber = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .cashier(cashier)
                .customer(customer)
                .prescriptionReference(request.getPrescriptionReference())
                .prescribingDoctor(request.getDoctorName())
                .patientName(request.getPatientName())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH)
                .paymentReference(request.getPaymentReference())
                .paymentStatus(PaymentStatus.PAID)
                .notes(request.getNotes())
                .subtotalAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        BigDecimal vatableNet = BigDecimal.ZERO;
        BigDecimal totalVat = BigDecimal.ZERO;
        BigDecimal totalExempt = BigDecimal.ZERO;
        BigDecimal totalZeroRated = BigDecimal.ZERO;

        List<CheckoutResponse.CheckoutItemDetail> itemDetails = new ArrayList<>();
        List<DigiTaxInvoicePayload.ItemInfo> digiTaxItems = new ArrayList<>();

        for (CartItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found ID: " + itemReq.getProductId()));

            // FEFO batch allocation & atomic inventory deduction
            InventoryFefoService.AllocatedBatchAllocation allocation =
                    inventoryFefoService.allocateBatchFefo(product.getProductId(), itemReq.getUnitId(), itemReq.getQuantity());

            BigDecimal discount = itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO;
            TaxClassification taxClass = product.getTaxClassification();

            TaxEngineService.LineTaxCalculation lineTax = taxEngineService.calculateTaxFromInclusivePrice(
                    allocation.unitPrice(), allocation.quantitySoldInUnit(), discount, taxClass
            );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .batch(allocation.batch())
                    .unit(allocation.unit())
                    .quantity(allocation.quantitySoldInUnit())
                    .quantityInBaseUnits(allocation.baseUnitsDeducted())
                    .unitPrice(allocation.unitPrice())
                    .discount(discount)
                    .taxClassification(taxClass)
                    .taxRate(taxClass.getTaxRate())
                    .taxAmount(lineTax.taxAmount())
                    .lineTotal(lineTax.grossAmount())
                    .build();

            order.getItems().add(orderItem);

            subtotal = subtotal.add(allocation.unitPrice().multiply(BigDecimal.valueOf(allocation.quantitySoldInUnit())));
            totalDiscount = totalDiscount.add(discount);
            totalTax = totalTax.add(lineTax.taxAmount());

            // Track tax breakdown for eTIMS
            if ("VAT_16".equalsIgnoreCase(taxClass.getClassCode())) {
                vatableNet = vatableNet.add(lineTax.netAmount());
                totalVat = totalVat.add(lineTax.taxAmount());
            } else if ("EXEMPT".equalsIgnoreCase(taxClass.getClassCode())) {
                totalExempt = totalExempt.add(lineTax.grossAmount());
            } else if ("ZERO_RATED".equalsIgnoreCase(taxClass.getClassCode())) {
                totalZeroRated = totalZeroRated.add(lineTax.grossAmount());
            }

            itemDetails.add(new CheckoutResponse.CheckoutItemDetail(
                    product.getBrandName(),
                    allocation.unit().getName(),
                    allocation.quantitySoldInUnit(),
                    allocation.batch().getBatchNumber(),
                    allocation.batch().getExpiryDate().toString(),
                    allocation.unitPrice(),
                    taxClass.getClassCode(),
                    lineTax.taxAmount(),
                    lineTax.grossAmount()
            ));

            digiTaxItems.add(DigiTaxInvoicePayload.ItemInfo.builder()
                    .code(product.getSku())
                    .name(product.getBrandName())
                    .hsCode(product.getHsCode())
                    .taxType(taxClass.getClassCode())
                    .quantity(allocation.quantitySoldInUnit())
                    .unitPrice(allocation.unitPrice())
                    .discount(discount)
                    .build());
        }

        BigDecimal grandTotal = subtotal.subtract(totalDiscount).max(BigDecimal.ZERO);
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(totalDiscount);
        order.setTaxAmount(totalTax);
        order.setTotalAmount(grandTotal);

        order = orderRepository.save(order);

        // Prepare DigiTax Invoice payload
        String buyerPin = request.getBuyerPin();
        if (buyerPin == null && customer != null) {
            buyerPin = customer.getKraPin();
        }
        String buyerName = request.getBuyerName() != null ? request.getBuyerName() :
                (customer != null ? customer.getFullName() : "Walk-in Retail");

        DigiTaxInvoicePayload digiTaxPayload = DigiTaxInvoicePayload.builder()
                .branchCode("MAIN_BRANCH")
                .cashier(cashier.getFullName())
                .buyer(DigiTaxInvoicePayload.BuyerInfo.builder()
                        .pin(buyerPin)
                        .name(buyerName)
                        .phone(request.getBuyerPhone())
                        .build())
                .items(digiTaxItems)
                .payment(DigiTaxInvoicePayload.PaymentInfo.builder()
                        .method(order.getPaymentMethod().name())
                        .reference(order.getPaymentReference())
                        .amountPaid(grandTotal)
                        .build())
                .build();

        // Submit to DigiTax API
        DigiTaxResponse digiTaxResponse = digiTaxGatewayService.fiscalizeInvoice(digiTaxPayload);

        FiscalInvoice fiscalInvoice = FiscalInvoice.builder()
                .order(order)
                .branchCode("MAIN_BRANCH")
                .cashierName(cashier.getFullName())
                .buyerPin(buyerPin)
                .buyerName(buyerName)
                .buyerPhone(request.getBuyerPhone())
                .totalTaxableAmount(vatableNet)
                .totalExemptAmount(totalExempt)
                .totalZeroRatedAmount(totalZeroRated)
                .totalVatAmount(totalVat)
                .grandTotal(grandTotal)
                .build();

        if ("SUCCESS".equalsIgnoreCase(digiTaxResponse.getStatus()) && digiTaxResponse.getData() != null) {
            fiscalInvoice.setFiscalStatus(FiscalStatus.COMPLETED);
            fiscalInvoice.setKraControlUnitNumber(digiTaxResponse.getData().getKraControlUnitNumber());
            fiscalInvoice.setKraInvoiceNumber(digiTaxResponse.getData().getKraInvoiceNumber());
            fiscalInvoice.setQrCodeString(digiTaxResponse.getData().getQrCodeString());
            fiscalInvoice.setQrCodeUrl(digiTaxResponse.getData().getQrCodeUrl());
            fiscalInvoice.setFiscalizedAt(LocalDateTime.now());
        } else {
            fiscalInvoice.setFiscalStatus(FiscalStatus.QUEUED);
            fiscalInvoice.setLastErrorMessage(digiTaxResponse.getMessage());
        }

        fiscalInvoice = fiscalInvoiceRepository.save(fiscalInvoice);
        order.setFiscalInvoice(fiscalInvoice);

        // Generate ESC/POS printable receipt
        String formattedReceipt = receiptPrinterService.buildThermalReceiptText(order, fiscalInvoice);

        return CheckoutResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getCreatedAt())
                .cashierName(cashier.getFullName())
                .customerName(buyerName)
                .subtotal(subtotal)
                .discount(totalDiscount)
                .vatableNet(vatableNet)
                .vatAmount(totalVat)
                .exemptAmount(totalExempt)
                .zeroRatedAmount(totalZeroRated)
                .totalAmount(grandTotal)
                .paymentMethod(order.getPaymentMethod().name())
                .paymentReference(order.getPaymentReference())
                .fiscalStatus(fiscalInvoice.getFiscalStatus())
                .kraControlUnitNumber(fiscalInvoice.getKraControlUnitNumber())
                .kraInvoiceNumber(fiscalInvoice.getKraInvoiceNumber())
                .qrCodeString(fiscalInvoice.getQrCodeString())
                .qrCodeUrl(fiscalInvoice.getQrCodeUrl())
                .fiscalMessage(digiTaxResponse.getMessage())
                .formattedReceipt(formattedReceipt)
                .items(itemDetails)
                .build();
    }
}

