package com.checkout.payment.gateway.configuration;

import com.checkout.payment.gateway.exception.BankServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class BankErrorDecoder implements ErrorDecoder {
  private final ErrorDecoder defaultDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    var status = response.status();
    if (status >= 500 && status < 600) {
      return new BankServiceUnavailableException(
          String.format(
              "Unable to reach bank service. HTTP %d. Response body: %s",
              status,
              response.body()
          )
      );
    }
    return defaultDecoder.decode(methodKey, response);
  }
}
