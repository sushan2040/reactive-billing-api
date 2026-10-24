package com.billing.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InvoiceRequest(
        @NotBlank @Size(max = 100)
        String customerName,

        @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2)
        BigDecimal amount,

        @NotBlank @Pattern(regexp = "INR|USD|EUR", message = "must be one of INR, USD, EUR")
        String currency) {}
