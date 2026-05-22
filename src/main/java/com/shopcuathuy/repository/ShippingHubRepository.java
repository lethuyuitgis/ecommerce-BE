package com.shopcuathuy.repository;

import com.shopcuathuy.entity.ShippingHub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingHubRepository extends JpaRepository<ShippingHub, String> {
}
