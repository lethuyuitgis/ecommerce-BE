package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.ConversationDTO;
import com.shopcuathuy.dto.CreateMessageRequest;
import com.shopcuathuy.dto.MessageDTO;
import com.shopcuathuy.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> listConversations(
            @RequestHeader("X-User-Id") String userId) {
        List<ConversationDTO> conversations = messageService.getSellerConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getConversationMessages(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageDTO> messages = messageService.getConversationMessages(userId, conversationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateMessageRequest request) {
        MessageDTO message = messageService.sendMessageAsSeller(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent", message));
    }
}
