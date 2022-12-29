package com.niteshkumarportfolio.PaymentService.service;

import com.niteshkumarportfolio.PaymentService.model.PaymentRequest;
import com.niteshkumarportfolio.PaymentService.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
