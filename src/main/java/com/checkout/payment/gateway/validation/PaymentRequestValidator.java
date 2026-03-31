package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.enums.CurrencyCode;
import com.checkout.payment.gateway.model.PaymentRequest;
import java.time.DateTimeException;
import java.time.YearMonth;

public class PaymentRequestValidator {
  public static boolean isValid(PaymentRequest request) {
    var validCardNumber = validateCardNumber(request.cardNumber());
    var validExpiryMonth = validateExpiryMonth(request.expiryMonth());
    var validExpiry = validateExpiry(request.expiryYear(), request.expiryMonth());
    var validCurrency = validateCurrency(request.currency());
    var validCvv = validateCvv(request.cvv());
    return validCardNumber && validExpiryMonth && validExpiry && validCurrency && validCvv;
  }

  private static boolean validateCardNumber(String cardNumber) {
    return !(cardNumber == null) && cardNumber.matches("\\d{14,19}");
  }

  private static boolean validateExpiryMonth(int expiryMonth) {
    return (expiryMonth >= 1 && expiryMonth <= 12);
  }

  private static boolean validateExpiry(int expiryYear, int expiryMonth) {
    try {
      YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
      return expiry.isAfter(YearMonth.now());
    } catch (DateTimeException e) {
      return false;
    }
  }

  private static boolean validateCurrency(String currency) {
    return CurrencyCode.GBP.toString().equals(currency) ||
        CurrencyCode.USD.toString().equals(currency) ||
        CurrencyCode.EUR.toString().equals(currency);
  }

  private static boolean validateCvv(String cvv) {
    return !(cvv == null) && cvv.matches("\\d{3,4}");
  }
}
