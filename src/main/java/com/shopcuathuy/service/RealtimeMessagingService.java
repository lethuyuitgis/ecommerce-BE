package com.shopcuathuy.service;

import com.shopcuathuy.dto.response.ChatMessageResponseDTO;
import com.shopcuathuy.dto.response.NotificationResponseDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeMessagingService {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeMessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendChatMessage(String conversationId, ChatMessageResponseDTO payload) {
        if (conversationId == null || payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, payload);
    }

    public void sendNotification(String userId, NotificationResponseDTO payload) {
        if (userId == null || payload == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/users/" + userId, payload);
    }
}




