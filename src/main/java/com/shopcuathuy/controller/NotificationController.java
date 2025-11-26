package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.response.NotificationPageResponseDTO;
import com.shopcuathuy.dto.response.NotificationResponseDTO;
import com.shopcuathuy.entity.Notification;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.NotificationRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository,
                                  UserRepository userRepository,
                                  NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPageResponseDTO>> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository
            .findByRecipientIdOrderByCreatedAtDesc(userId, pageable);

        NotificationPageResponseDTO response = new NotificationPageResponseDTO();
        response.setContent(notificationPage.getContent().stream()
            .map(notificationService::toResponseDTO)
            .toList());
        response.setTotalElements(notificationPage.getTotalElements());
        response.setTotalPages(notificationPage.getTotalPages());
        response.setSize(notificationPage.getSize());
        response.setNumber(notificationPage.getNumber());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @RequestParam String userId) {
        
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(userId);
        return ResponseEntity.ok(ApiResponse.success((int) unreadCount));
    }

    @PostMapping("/{id}/read")
    @Transactional
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> markAsRead(
            @PathVariable String id,
            @RequestParam String userId) {
        
        userRepository.findById(userId)
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

        NotificationResponseDTO dto = notificationService.toResponseDTO(notification);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/read-all")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestParam String userId) {
        
        userRepository.findById(userId)
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
        
        userRepository.findById(userId)
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

}

