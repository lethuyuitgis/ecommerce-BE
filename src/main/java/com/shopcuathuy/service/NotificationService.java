package com.shopcuathuy.service;

import com.shopcuathuy.dto.response.NotificationResponseDTO;
import com.shopcuathuy.entity.Notification;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.repository.NotificationRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RealtimeMessagingService realtimeMessagingService;

    public NotificationService(NotificationRepository notificationRepository,
                               RealtimeMessagingService realtimeMessagingService) {
        this.notificationRepository = notificationRepository;
        this.realtimeMessagingService = realtimeMessagingService;
    }

    public NotificationResponseDTO createAndDispatch(User recipient,
                                                     Notification.NotificationType type,
                                                     String title,
                                                     String message,
                                                     String linkUrl,
                                                     String relatedId,
                                                     String imageUrl) {
        if (recipient == null || type == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title != null ? title : type.name());
        notification.setMessage(message);
        notification.setLinkUrl(linkUrl);
        notification.setRelatedId(relatedId);
        notification.setImageUrl(imageUrl);

        Notification saved = notificationRepository.save(notification);
        NotificationResponseDTO dto = toResponseDTO(saved);
        realtimeMessagingService.sendNotification(recipient.getId(), dto);
        return dto;
    }

    public Optional<NotificationResponseDTO> markAsRead(String notificationId) {
        return notificationRepository.findById(notificationId)
            .map(notification -> {
                notification.setIsRead(true);
                notification.setReadAt(java.time.LocalDateTime.now());
                notificationRepository.save(notification);
                return toResponseDTO(notification);
            });
    }

    public NotificationResponseDTO toResponseDTO(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getRecipient() != null ? notification.getRecipient().getId() : null);
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType() != null ? notification.getType().name() : null);
        dto.setLinkUrl(notification.getLinkUrl());
        dto.setImageUrl(notification.getImageUrl());
        dto.setRead(Boolean.TRUE.equals(notification.getIsRead()));
        if (notification.getCreatedAt() != null) {
            dto.setCreatedAt(notification.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        } else {
            dto.setCreatedAt(Instant.now());
        }
        return dto;
    }
}

