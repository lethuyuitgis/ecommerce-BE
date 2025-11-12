package com.shopcuathuy.repository;

import com.shopcuathuy.entity.ShippingMethod;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, String> {
    List<ShippingMethod> findByIsActiveTrue();
}








