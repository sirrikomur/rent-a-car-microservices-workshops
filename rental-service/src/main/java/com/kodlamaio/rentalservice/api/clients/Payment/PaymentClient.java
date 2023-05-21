package com.kodlamaio.rentalservice.api.clients.Payment;

import com.kodlamaio.commonpackage.utils.dto.ClientResponse;
import com.kodlamaio.commonpackage.utils.dto.CreateRentalPaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", fallback = PaymentClientFallback.class)
public interface PaymentClient {
    @PutMapping(value = "/api/payments")
    ClientResponse processRentalPayment(@RequestBody CreateRentalPaymentRequest request);
}
