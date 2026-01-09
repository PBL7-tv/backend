package com.pbl7.payment_service.dto.request;

import com.pbl7.payment_service.dto.AddressDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

    Long addressId;
    AddressDTO deliveryAddress;

}
