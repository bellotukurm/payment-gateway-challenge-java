package com.checkout.payment.gateway.clients;

import com.checkout.payment.gateway.configuration.BankClientConfig;
import com.checkout.payment.gateway.model.AuthorizationResponse;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bankClient", configuration = BankClientConfig.class)
public interface BankClient {
    @PostMapping("/payments")
    AuthorizationResponse processPayments(@RequestBody BankPaymentRequest request);
}
