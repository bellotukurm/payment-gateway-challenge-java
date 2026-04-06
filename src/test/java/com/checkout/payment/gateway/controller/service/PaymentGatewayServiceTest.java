package com.checkout.payment.gateway.controller.service;

import com.checkout.payment.gateway.clients.BankClient;
import com.checkout.payment.gateway.enums.CurrencyCode;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.AuthorizationResponse;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayServiceTest {
  @Mock
  private BankClient bankClient;

  @Mock
  private PaymentsRepository paymentsRepository;

  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  @Test
  void getPaymentById_shouldReturnPayment_whenPaymentExists() {
    var paymentId = UUID.randomUUID();
    var expectedResponse = new PostPaymentResponse(
        paymentId,
        PaymentStatus.AUTHORIZED,
        "1111",
        12,
        2030,
        CurrencyCode.GBP.toString(),
        100
    );

    when(paymentsRepository.get(paymentId)).thenReturn(Optional.of(expectedResponse));

    PostPaymentResponse actualResponse = paymentGatewayService.getPaymentById(paymentId);

    assertEquals(expectedResponse, actualResponse);
    verify(paymentsRepository).get(paymentId);
  }

  @Test
  void getPaymentById_shouldThrowException_whenPaymentDoesNotExist() {
    UUID paymentId = UUID.randomUUID();
    when(paymentsRepository.get(paymentId)).thenReturn(Optional.empty());

    assertThrows(EventProcessingException.class, () -> paymentGatewayService.getPaymentById(paymentId));
    verify(paymentsRepository).get(paymentId);
  }

  @Test
  void processPayment_shouldReturnAuthorized_whenRequestIsValidAndBankApproves() {
    var authResponse = new AuthorizationResponse(true, "code");

    var cardNumber = "4111111111111111";
    var expiryMonth = 12;
    var expiryYear = 2030;
    var expiryDate = "12/2030";
    var currencyCode = CurrencyCode.GBP.toString();
    var amount = 100;
    var cvv = "123";

    when(bankClient.processPayments(any(BankPaymentRequest.class))).thenReturn(authResponse);

    var validPaymentRequest = new PaymentRequest(
        cardNumber,
        expiryMonth,
        expiryYear,
        currencyCode,
        amount,
        cvv
    );

    var response = paymentGatewayService.processPayment(validPaymentRequest);

    assertNotNull(response.getId());
    assertEquals(PaymentStatus.AUTHORIZED, response.getStatus());
    assertEquals("1111", response.getCardNumberLastFour());
    assertEquals(expiryMonth, response.getExpiryMonth());
    assertEquals(expiryYear, response.getExpiryYear());
    assertEquals(currencyCode, response.getCurrency());
    assertEquals(amount, response.getAmount());

    var captor = ArgumentCaptor.forClass(BankPaymentRequest.class);
    verify(bankClient).processPayments(captor.capture());

    var bankRequest = captor.getValue();
    assertEquals(cardNumber, bankRequest.cardNumber());
    assertEquals(expiryDate, bankRequest.expiryDate());
    assertEquals(currencyCode, bankRequest.currency());
    assertEquals(amount, bankRequest.amount());
    assertEquals(cvv, bankRequest.cvv());

    verify(paymentsRepository).add(response);
  }

  @Test
  void processPayment_shouldReturnDeclined_whenRequestIsValidAndBankDeclines() {
    var authResponse = new AuthorizationResponse(false, "code");

    var cardNumber = "4111111111111111";
    var expiryMonth = 12;
    var expiryYear = 2030;
    var expiryDate = "12/2030";
    var currencyCode = CurrencyCode.GBP.toString();
    var amount = 100;
    var cvv = "123";

    when(bankClient.processPayments(any(BankPaymentRequest.class))).thenReturn(authResponse);

    var validPaymentRequest = new PaymentRequest(
        cardNumber,
        expiryMonth,
        expiryYear,
        currencyCode,
        amount,
        cvv
    );

    var response = paymentGatewayService.processPayment(validPaymentRequest);

    assertNotNull(response.getId());
    assertEquals(PaymentStatus.DECLINED, response.getStatus());
    assertEquals("1111", response.getCardNumberLastFour());
    assertEquals(expiryMonth, response.getExpiryMonth());
    assertEquals(expiryYear, response.getExpiryYear());
    assertEquals(currencyCode, response.getCurrency());
    assertEquals(amount, response.getAmount());

    var captor = ArgumentCaptor.forClass(BankPaymentRequest.class);
    verify(bankClient).processPayments(captor.capture());

    var bankRequest = captor.getValue();
    assertEquals(cardNumber, bankRequest.cardNumber());
    assertEquals(expiryDate, bankRequest.expiryDate());
    assertEquals(currencyCode, bankRequest.currency());
    assertEquals(amount, bankRequest.amount());
    assertEquals(cvv, bankRequest.cvv());

    verify(paymentsRepository).add(response);
  }

  @Test
  void processPayment_shouldReturnRejected_whenRequestIsInvalid() {
    var cardNumber = "4111111111111111";
    var expiryMonth = 13;
    var expiryYear = 2030;
    var currencyCode = CurrencyCode.GBP.toString();
    var amount = 100;
    var cvv = "12";

    var invalidPaymentRequest = new PaymentRequest(
        cardNumber,
        expiryMonth,
        expiryYear,
        currencyCode,
        amount,
        cvv
    );

    var response = paymentGatewayService.processPayment(invalidPaymentRequest);

    assertNotNull(response.getId());
    assertEquals(PaymentStatus.REJECTED, response.getStatus());
    assertEquals("1111", response.getCardNumberLastFour());
    assertEquals(expiryMonth, response.getExpiryMonth());
    assertEquals(expiryYear, response.getExpiryYear());
    assertEquals(currencyCode, response.getCurrency());
    assertEquals(amount, response.getAmount());

    verify(bankClient, never()).processPayments(any(BankPaymentRequest.class));
    verify(paymentsRepository).add(response);
  }
}
