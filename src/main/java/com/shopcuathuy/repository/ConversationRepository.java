package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT c FROM Conversation c WHERE c.seller.user.id = :sellerUserId ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findBySellerUserIdOrdered(String sellerUserId);

    Optional<Conversation> findBySellerIdAndCustomerId(String sellerId, String customerId);
}
