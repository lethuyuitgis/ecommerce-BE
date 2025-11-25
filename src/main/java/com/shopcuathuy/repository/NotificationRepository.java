package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);
    long countByRecipientIdAndIsReadFalse(String recipientId);
    List<Notification> findByRecipientId(String recipientId);
}

