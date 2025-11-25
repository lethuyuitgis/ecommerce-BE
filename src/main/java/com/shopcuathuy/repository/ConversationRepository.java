package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT DISTINCT c FROM Conversation c " +
           "LEFT JOIN FETCH c.seller s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH c.customer " +
           "WHERE s.user.id = :sellerUserId " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findBySellerUserIdOrdered(String sellerUserId);

    @Query("SELECT DISTINCT c FROM Conversation c " +
           "LEFT JOIN FETCH c.seller s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH c.customer " +
           "WHERE c.seller.id = :sellerId " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findBySellerIdOrdered(String sellerId);

    Optional<Conversation> findBySellerIdAndCustomerId(String sellerId, String customerId);
}
