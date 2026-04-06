package com.checkout.payment.gateway.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.clients.BankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.AuthorizationResponse;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BankClient bankClient;

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment = new PostPaymentResponse();
    payment.setId(UUID.randomUUID());
    payment.setAmount(10);
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2024);
    payment.setCardNumberLastFour(4321);

    paymentsRepository.add(payment);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Page not found"));
  }

  @Test
  void whenValidPaymentRequestIsPostedThenProcessedPaymentIsReturned() throws Exception {
    PaymentRequest paymentRequest = new PaymentRequest(
        "4444333322224321",
        12,
        2029,
        "USD",
        10,
        "123"
    );

    var bankResponse = new AuthorizationResponse(true, "code");
    when(bankClient.processPayments(any(BankPaymentRequest.class))).thenReturn(bankResponse);

    mvc.perform(MockMvcRequestBuilders.post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(paymentRequest)))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.status").value(PaymentStatus.AUTHORIZED.getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(4321))
        .andExpect(jsonPath("$.expiryMonth").value(paymentRequest.expiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(paymentRequest.expiryYear()))
        .andExpect(jsonPath("$.currency").value(paymentRequest.currency()))
        .andExpect(jsonPath("$.amount").value(paymentRequest.amount()));
  }
}
