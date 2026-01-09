package com.pbl7.identity_service.mapper;

import com.pbl7.identity_service.dto.AddressDTO;
import com.pbl7.identity_service.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddress(AddressDTO dto);
    AddressDTO toDto(Address entity);
}

