package com.pharmacore.pos.service;

import com.pharmacore.pos.entity.TaxClassification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TaxEngineService {

    public record LineTaxCalculation(
            BigDecimal netAmount,
            BigDecimal taxAmount,
            BigDecimal grossAmount
    ) {}

    /**
     * Calculates tax from inclusive price.
     * E.g., for 16% VAT and gross 116.00:
     * Net = 116.00 / 1.16 = 100.00
     * Tax = 16.00
     */
    public LineTaxCalculation calculateTaxFromInclusivePrice(
            BigDecimal unitPrice, 
            int quantity, 
            BigDecimal discount, 
            TaxClassification taxClass
    ) {
        BigDecimal totalGross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            totalGross = totalGross.subtract(discount).max(BigDecimal.ZERO);
        }

        BigDecimal rate = taxClass.getTaxRate();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            // EXEMPT or ZERO_RATED (0.00% VAT)
            return new LineTaxCalculation(totalGross, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), totalGross);
        }

        // Standard VAT (e.g. 16%): factor = 1 + (16 / 100) = 1.16
        BigDecimal divisor = BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal netAmount = totalGross.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = totalGross.subtract(netAmount).setScale(2, RoundingMode.HALF_UP);

        return new LineTaxCalculation(netAmount, taxAmount, totalGross);
    }
}

