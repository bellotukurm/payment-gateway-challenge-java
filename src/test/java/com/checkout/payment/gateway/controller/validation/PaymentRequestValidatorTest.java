package com.checkout.payment.gateway.controller.validation;

import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.validation.PaymentRequestValidator;
import org.junit.jupiter.api.Test;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentRequestValidatorTest {

  @Test
  void shouldReturnTrueForValidRequest() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        "123"
    );

    assertTrue(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForNullCardNumber() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        null,
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForCardNumberTooShort() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "4242424242424",
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForCardNumberTooLong() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242424242",
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForCardNumberWithNonDigits() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "4242abcd424242",
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForExpiryMonthLessThanOne() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        0,
        future.getYear(),
        "GBP",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForExpiryMonthGreaterThanTwelve() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        13,
        future.getYear(),
        "GBP",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForExpiredCard() {
    YearMonth past = YearMonth.now().minusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        past.getMonthValue(),
        past.getYear(),
        "GBP",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForCurrentMonthExpiry() {
    YearMonth current = YearMonth.now();
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        current.getMonthValue(),
        current.getYear(),
        "GBP",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForUnsupportedCurrency() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        future.getMonthValue(),
        future.getYear(),
        "AUD",
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldThrowForNullCurrency() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        future.getMonthValue(),
        future.getYear(),
        null,
        100,
        "123"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForNullCvv() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        null
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForCvvTooShort() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        "12"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForCvvTooLong() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        "12345"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }

  @Test
  void shouldReturnFalseForCvvWithNonDigits() {
    YearMonth future = YearMonth.now().plusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "42424242424242",
        future.getMonthValue(),
        future.getYear(),
        "GBP",
        100,
        "12a"
    );

    assertFalse(PaymentRequestValidator.isValid(request));
  }
}
