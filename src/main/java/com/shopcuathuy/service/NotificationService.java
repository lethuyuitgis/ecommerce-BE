package com.shopcuathuy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcuathuy.dto.NotificationDTO;
import com.shopcuathuy.entity.Notification;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.NotificationRepository;
import com.shopcuathuy.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final String USER_TOPIC_PREFIX = "/topic/users/";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public Page<NotificationDTO> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDTO);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    public void markAsRead(String userId, String notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        LocalDateTime now = LocalDateTime.now();
        boolean updated = false;
        for (Notification notification : notifications) {
            if (!Boolean.TRUE.equals(notification.getIsRead())) {
                notification.setIsRead(true);
                notification.setReadAt(now);
                updated = true;
            }
        }
        if (updated) {
            notificationRepository.saveAll(notifications);
        }
    }

    public void deleteNotification(String userId, String notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notificationRepository.delete(notification);
    }

    public NotificationDTO createNotificationForUser(
            String userId,
            Notification.NotificationType type,
            String title,
            String message,
            String relatedId,
            String linkUrl,
            String imageUrl,
            Map<String, Object> data
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedId(relatedId);
        notification.setLinkUrl(linkUrl);
        notification.setImageUrl(imageUrl);
        notification.setData(serializeData(data));
        notification.setIsRead(false);
        notification.setReadAt(null);

        Notification saved = notificationRepository.save(notification);
        NotificationDTO dto = toDTO(saved);
        sendRealtimeUpdate(userId, dto);
        return dto;
    }

    public void notifyUsersByRole(
            User.UserType userType,
            Notification.NotificationType type,
            String title,
            String message,
            String relatedId,
            String linkUrl,
            String imageUrl,
            Map<String, Object> data
    ) {
        List<User> recipients = userRepository.findByUserType(userType);
        if (recipients.isEmpty()) {
            return;
        }

        for (User recipient : recipients) {
            try {
                createNotificationForUser(
                        recipient.getId(),
                        type,
                        title,
                        message,
                        relatedId,
                        linkUrl,
                        imageUrl,
                        data
                );
            } catch (Exception ex) {
                log.warn("Failed to create notification for user {}", recipient.getId(), ex);
            }
        }
    }

    public void notifyOrderCreated(Order order) {
        if (order.getSeller() == null || order.getSeller().getUser() == null) {
            return;
        }

        String sellerUserId = order.getSeller().getUser().getId();
        String orderId = order.getId();
        String orderNumber = order.getOrderNumber();

        String title = "Đơn hàng mới";
        String message = String.format("Bạn có đơn hàng mới #%s", orderNumber);
        Map<String, Object> data = Map.of(
                "orderId", orderId,
                "orderNumber", orderNumber,
                "status", order.getStatus().name()
        );

        createNotificationForUser(
                sellerUserId,
                Notification.NotificationType.ORDER_NEW,
                title,
                message,
                orderId,
                String.format("/seller/orders/%s", orderId),
                null,
                data
        );
    }

    public void notifyOrderStatusChanged(Order order, Order.OrderStatus newStatus, String actorUserId) {
        String orderId = order.getId();
        String orderNumber = order.getOrderNumber();
        String statusLabel = mapOrderStatusToLabel(newStatus);
        String statusCode = mapOrderStatusToFrontendCode(newStatus);

        Map<String, Object> data = Map.of(
                "orderId", orderId,
                "orderNumber", orderNumber,
                "status", statusCode
        );

        // Notify customer
        String customerUserId = order.getCustomer().getId();
        String customerMessage = String.format("Đơn hàng #%s đã chuyển sang trạng thái %s", orderNumber, statusLabel);
        createNotificationForUser(
                customerUserId,
                Notification.NotificationType.ORDER_STATUS,
                "Cập nhật đơn hàng",
                customerMessage,
                orderId,
                String.format("/orders/%s", orderId),
                null,
                data
        );

        // Notify seller (skip if seller is actor and status change is triggered by them)
        if (order.getSeller() != null && order.getSeller().getUser() != null) {
            String sellerUserId = order.getSeller().getUser().getId();
            if (!sellerUserId.equals(actorUserId) || newStatus == Order.OrderStatus.DELIVERED) {
                String sellerMessage = String.format("Đơn hàng #%s đã được cập nhật trạng thái %s", orderNumber, statusLabel);
                createNotificationForUser(
                        sellerUserId,
                        Notification.NotificationType.ORDER_STATUS,
                        "Cập nhật đơn hàng",
                        sellerMessage,
                        orderId,
                        String.format("/seller/orders/%s", orderId),
                        null,
                        data
                );
            }
        }
    }

    private String serializeData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize notification data", e);
            return null;
        }
    }

    private void sendRealtimeUpdate(String userId, NotificationDTO dto) {
        try {
            messagingTemplate.convertAndSend(USER_TOPIC_PREFIX + userId, dto);
        } catch (Exception ex) {
            log.debug("Failed to send websocket notification to user {}", userId, ex);
        }
    }

    private String mapOrderStatusToFrontendCode(Order.OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "PROCESSING";
            case SHIPPED -> "SHIPPING";
            case DELIVERED -> "DELIVERED";
            case CANCELLED -> "CANCELLED";
            default -> status.name();
        };
    }

    private String mapOrderStatusToLabel(Order.OrderStatus status) {
        return switch (status) {
            case PENDING -> "chờ xác nhận";
            case CONFIRMED -> "đang xử lý";
            case SHIPPED -> "đang giao";
            case DELIVERED -> "đã giao";
            case CANCELLED -> "đã hủy";
            case RETURNED -> "đã trả hàng";
        };
    }

    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType().name());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRelatedId(notification.getRelatedId());
        dto.setLinkUrl(notification.getLinkUrl());
        dto.setImageUrl(notification.getImageUrl());
        dto.setData(notification.getData());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
