package com.shopcuathuy.repository;

import com.shopcuathuy.entity.SellerNotificationPreference;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerNotificationPreferenceRepository extends JpaRepository<SellerNotificationPreference, String> {

    List<SellerNotificationPreference> findBySellerId(String sellerId);

    Optional<SellerNotificationPreference> findBySellerIdAndType(String sellerId, String type);
}
