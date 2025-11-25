package com.shopcuathuy.repository;

import com.shopcuathuy.entity.UserAddress;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, String> {
    List<UserAddress> findByUserId(String userId);

    List<UserAddress> findByUserIdIn(Collection<String> userIds);
}








