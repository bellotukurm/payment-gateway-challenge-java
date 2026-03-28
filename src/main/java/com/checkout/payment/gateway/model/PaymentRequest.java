package com.checkout.payment.gateway.model;

public record PaymentRequest(
    String cardNumber,
    int expiryMonth,
    int expiryYear,
    String currency,
    int amount,
    String cvv
) { }
