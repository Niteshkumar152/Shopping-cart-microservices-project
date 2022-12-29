package com.niteshkumarportfolio.OrderService.service;

import com.niteshkumarportfolio.OrderService.model.OrderRequest;
import com.niteshkumarportfolio.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
