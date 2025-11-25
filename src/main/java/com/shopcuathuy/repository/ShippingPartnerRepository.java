package com.shopcuathuy.repository;

import com.shopcuathuy.entity.ShippingPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingPartnerRepository extends JpaRepository<ShippingPartner, String> {
}


