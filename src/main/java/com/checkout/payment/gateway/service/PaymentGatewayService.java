package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.clients.BankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final BankClient bankClient;
  private final PaymentsRepository paymentsRepository;

  public PaymentGatewayService(
      BankClient bankClient,
      PaymentsRepository paymentsRepository
  ) {
    this.bankClient = bankClient;
    this.paymentsRepository = paymentsRepository;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PostPaymentResponse processPayment(PaymentRequest paymentRequest) {
    var bankPaymentRequest = createBankPaymentRequest(paymentRequest);
    var paymentStatus = getPaymentStatus(paymentRequest, bankPaymentRequest);
    var postPaymentResponse = createPostPaymentResponse(paymentRequest, paymentStatus);

    paymentsRepository.add(postPaymentResponse);
    return postPaymentResponse;
  }

  private BankPaymentRequest createBankPaymentRequest(PaymentRequest paymentRequest) {
    var expiryMonth = String.format("%02d", paymentRequest.getExpiryMonth());
    var expiryYear = Integer.toString(paymentRequest.getExpiryYear());
    var expiryDate = String.join("/", expiryMonth, expiryYear);
    return new BankPaymentRequest(
        paymentRequest.getCardNumber(),
        expiryDate,
        paymentRequest.getCurrency(),
        paymentRequest.getAmount(),
        paymentRequest.getCvv()
    );
  }

  private PaymentStatus getPaymentStatus(PaymentRequest paymentRequest, BankPaymentRequest bankPaymentRequest) {
    if (!PaymentRequestValidator.isValid(paymentRequest)) {
      return PaymentStatus.REJECTED;
    } else {
      var authResponse = bankClient.processPayments(bankPaymentRequest);
      return authResponse.authorized()
          ? PaymentStatus.AUTHORIZED
          : PaymentStatus.DECLINED;
    }
  }

  private PostPaymentResponse createPostPaymentResponse(PaymentRequest paymentRequest, PaymentStatus paymentStatus) {
    var last4 = paymentRequest.getCardNumber().length() >= 4
        ? paymentRequest.getCardNumber().substring(paymentRequest.getCardNumber().length() - 4)
        : paymentRequest.getCardNumber();
    var last4CardNumber = Integer.parseInt(last4);
    return new PostPaymentResponse(
        UUID.randomUUID(),
        paymentStatus,
        last4CardNumber,
        paymentRequest.getExpiryMonth(),
        paymentRequest.getExpiryYear(),
        paymentRequest.getCurrency(),
        paymentRequest.getAmount()
    );
  }
}
