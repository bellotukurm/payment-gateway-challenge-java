package com.checkout.payment.gateway.model;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import java.time.YearMonth;

@Getter
public class PaymentRequest {
    @NotNull
    @Pattern(regexp = "\\d{14,19}", message = "card number must be 14-19 digits")
    private final String cardNumber;

    @Min(value = 1, message = "expiry month must be between 1 and 12")
    @Max(value = 12, message = "expiry month must be between 1 and 12")
    private final int expiryMonth;

    private final int expiryYear;

    @NotNull
    private final String currency;

    private final int amount;

    @NotNull
    @Pattern(regexp = "\\d{3,4}", message = "cvv must be 3-4 digits")
    private final String cvv;


  public PaymentRequest(
      String cardNumber,
      int expiryMonth,
      int expiryYear,
      String currency,
      int amount, String cvv
  ) {
    this.cardNumber = cardNumber;
    this.expiryMonth = expiryMonth;
    this.expiryYear = expiryYear;
    validateExpiry();
    this.currency = currency;
    this.amount = amount;
    this.cvv = cvv;
  }

  private void validateExpiry() {
      YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
      if (!expiry.isAfter(YearMonth.now())) {
        throw new IllegalArgumentException("Card expiry must be in the future");
      }
  }
}
