package com.pbl7.order_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
     String id;
     String username;
     String email;
     Set<AddressDTO> shipAddress;
}
