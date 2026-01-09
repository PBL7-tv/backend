package com.pbl7.identity_service.repository;

import com.pbl7.identity_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByNameAndLocalityAndAddressAndCityAndStateAndMobile(
            String name, String locality, String address, String city, String state, String mobile);

}
