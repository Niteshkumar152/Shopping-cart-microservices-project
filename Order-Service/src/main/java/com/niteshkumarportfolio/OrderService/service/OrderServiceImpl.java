package com.niteshkumarportfolio.OrderService.service;

import com.niteshkumarportfolio.OrderService.entity.Order;
import com.niteshkumarportfolio.OrderService.exception.CustomException;
import com.niteshkumarportfolio.OrderService.external.client.PaymentService;
import com.niteshkumarportfolio.OrderService.external.client.ProductService;
import com.niteshkumarportfolio.OrderService.external.request.PaymentRequest;
import com.niteshkumarportfolio.OrderService.external.response.PaymentResponse;
import com.niteshkumarportfolio.OrderService.model.OrderRequest;
import com.niteshkumarportfolio.OrderService.model.OrderResponse;
import com.niteshkumarportfolio.OrderService.model.ProductResponse;
import com.niteshkumarportfolio.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;
    @Override
    public long placeOrder(OrderRequest orderRequest) {
        //Order Entity -> Save the data with Status Order Created
        //Product Service -> Block Products (Reduce the Quantity)
        //Payment Service -> Payments -> Success -> COMPLETE, Else
        //CANCELLED

        log.info("Placing Order Request: {}",orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();
        order = orderRepository.save(order);

        log.info("Calling Payment Service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest
                .builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;

        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the Order status to PLACED");
            orderStatus = "PLACED";
        }catch (Exception e){
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order Placed successfully with Order Id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get Order Details for Order Id:{}",orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new CustomException("Order not found for the order: "+orderId,"NOT_FOUND",404));

        log.info("Invoking Product service to fetch the product for id:{}",order.getProductId());

        ProductResponse productResponse =  restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(),
                ProductResponse.class);

        log.info("Getting payment information from the payment service");

        PaymentResponse paymentResponse = restTemplate.getForObject(
                "http://PAYMENT-SERVICE/payment/order/"+order.getId(),
                PaymentResponse.class
        );

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails
                .builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .quantity(productResponse.getQuantity())
                .price(productResponse.getPrice())
                .build();

        OrderResponse.PaymentDetails paymentDetails =
                OrderResponse.PaymentDetails
                        .builder()
                        .paymentId(paymentResponse.getPaymentId())
                        .paymentStatus(paymentResponse.getStatus())
                        .paymentDate(paymentResponse.getPaymentDate())
                        .paymentMode(paymentResponse.getPaymentMode())
                        .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
        return orderResponse;
    }
}
