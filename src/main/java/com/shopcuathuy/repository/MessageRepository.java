package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Message;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.readAt IS NULL AND m.sender.userType <> 'SELLER'")
    long countUnreadForSeller(String conversationId);

    List<Message> findTop20ByConversationIdAndReadAtIsNullOrderByCreatedAtDesc(String conversationId);
}
