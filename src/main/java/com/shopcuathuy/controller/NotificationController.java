package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Notification;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.NotificationRepository;
import com.shopcuathuy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository,
                                  UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository
            .findByRecipientIdOrderByCreatedAtDesc(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", notificationPage.getContent().stream().map(this::convertToDTO).toList());
        response.put("totalElements", notificationPage.getTotalElements());
        response.put("totalPages", notificationPage.getTotalPages());
        response.put("size", notificationPage.getSize());
        response.put("number", notificationPage.getNumber());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @RequestParam String userId) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(userId);
        return ResponseEntity.ok(ApiResponse.success((int) unreadCount));
    }

    @PostMapping("/{id}/read")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAsRead(
            @PathVariable String id,
            @RequestParam String userId) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        return ResponseEntity.ok(ApiResponse.success(convertToDTO(notification)));
    }

    @PostMapping("/read-all")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestParam String userId) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        notificationRepository.findByRecipientId(userId).forEach(notification -> {
            if (!notification.getIsRead()) {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        });

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable String id,
            @RequestParam String userId) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        notificationRepository.delete(notification);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> convertToDTO(Notification notification) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", notification.getId());
        dto.put("userId", notification.getRecipient() != null ? notification.getRecipient().getId() : null);
        dto.put("title", notification.getTitle());
        dto.put("message", notification.getMessage());
        dto.put("type", notification.getType() != null ? notification.getType().name() : null);
        dto.put("linkUrl", notification.getLinkUrl());
        dto.put("imageUrl", notification.getImageUrl());
        dto.put("isRead", notification.getIsRead());
        dto.put("createdAt", notification.getCreatedAt() != null ?
            notification.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return dto;
    }
}

