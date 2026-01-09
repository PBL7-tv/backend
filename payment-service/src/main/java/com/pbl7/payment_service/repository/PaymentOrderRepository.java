package com.pbl7.payment_service.repository;

import com.pbl7.payment_service.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    PaymentOrder findByPaymentLinkId(String paymentId);
    PaymentOrder findByOrderId(Long orderId);
}
