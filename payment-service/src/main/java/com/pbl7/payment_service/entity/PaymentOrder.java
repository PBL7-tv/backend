package com.pbl7.payment_service.entity;


import com.pbl7.payment_service.domain.PaymentMethod;
import com.pbl7.payment_service.domain.PaymentOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    Long amount;
    PaymentOrderStatus status = PaymentOrderStatus.PENDING;
    PaymentMethod paymentMethod;

    String paymentLinkId;

    String userId;

    Long orderId;
}
