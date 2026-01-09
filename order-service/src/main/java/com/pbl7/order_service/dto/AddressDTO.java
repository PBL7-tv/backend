package com.pbl7.order_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddressDTO)) return false;
        AddressDTO that = (AddressDTO) o;
        return Objects.equals(id, that.id) && // compare by ID to avoid issues
                Objects.equals(city, that.city) &&
                Objects.equals(address, that.address) &&
                Objects.equals(mobile, that.mobile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, city, address, mobile); // include id for unique address comparison
    }

}

