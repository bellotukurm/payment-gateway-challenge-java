package com.checkout.payment.gateway.configuration;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankClientConfig {
  @Bean
  public ErrorDecoder bankClientErrorDecoder() {
    return new BankErrorDecoder();
  }
}
