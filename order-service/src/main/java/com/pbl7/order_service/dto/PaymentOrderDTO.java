package com.pbl7.order_service.dto;


import com.pbl7.order_service.domain.PaymentMethod;
import com.pbl7.order_service.domain.PaymentOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentOrderDTO {

    Long id;

    Long amount;
    PaymentOrderStatus status = PaymentOrderStatus.PENDING;
    PaymentMethod paymentMethod;

    String paymentLinkId;

    String userId;

    @ElementCollection
    Set<Long> orderIds = new HashSet<>();

}
