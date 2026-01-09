package com.pbl7.order_service.dto.request;

import com.pbl7.order_service.dto.AddressDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

    Long addressId;
    AddressDTO deliveryAddress;

}
