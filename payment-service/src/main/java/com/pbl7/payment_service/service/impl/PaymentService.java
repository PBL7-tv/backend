package com.pbl7.payment_service.service.impl;


import com.pbl7.payment_service.dto.request.OrderRequest;
import com.pbl7.payment_service.entity.PaymentOrder;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentOrder createOrder(String userId, OrderRequest order);
    String getIdFromJwt(String jwt) throws Exception;
    PaymentOrder getPaymentOrderById(Long orderId) throws Exception ;
    PaymentOrder getPaymentOrderByPaymentId(String paymentId) throws Exception;
    Boolean proceedPayment(PaymentOrder paymentOrder, String paymentId, String paymentLinkId);
    String createPaymentLink(HttpServletRequest req, Long orderId, Long amount, String bankCode, String paymentLinkId);
}
