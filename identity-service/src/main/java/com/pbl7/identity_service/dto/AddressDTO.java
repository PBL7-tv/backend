package com.pbl7.identity_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressDTO {
    Long id;
    String name;
    String locality;
    String address;
    String city;
    String state;
    String mobile;
}
