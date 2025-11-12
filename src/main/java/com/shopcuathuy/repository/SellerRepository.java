package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Seller;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {
    Optional<Seller> findByUserId(String userId);
    boolean existsByUserId(String userId);
}

