package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.CreateMessageRequestDTO;
import com.shopcuathuy.dto.response.ChatMessageResponseDTO;
import com.shopcuathuy.dto.response.MessageConversationResponseDTO;
import com.shopcuathuy.dto.response.MessagePageResponseDTO;
import com.shopcuathuy.entity.Conversation;
import com.shopcuathuy.entity.Message;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.Notification;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ConversationRepository;
import com.shopcuathuy.repository.MessageRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.service.NotificationService;
import com.shopcuathuy.service.RealtimeMessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final RealtimeMessagingService realtimeMessagingService;
    private final NotificationService notificationService;

    @Autowired
    public MessageController(ConversationRepository conversationRepository,
                            MessageRepository messageRepository,
                            UserRepository userRepository,
                            SellerRepository sellerRepository,
                            RealtimeMessagingService realtimeMessagingService,
                            NotificationService notificationService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.realtimeMessagingService = realtimeMessagingService;
        this.notificationService = notificationService;
    }

    /* ===================== Customer Endpoints ===================== */

    @GetMapping("/messages/conversations")
    public ResponseEntity<ApiResponse<List<MessageConversationResponseDTO>>> getCustomerConversations(
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }

        List<Conversation> conversations = conversationRepository.findAll().stream()
            .filter(c -> c.getCustomer() != null && userId.equals(c.getCustomer().getId()))
            .collect(Collectors.toList());
        
        List<MessageConversationResponseDTO> conversationDTOs = conversations.stream()
            .map(this::convertConversationToDTO)
            .sorted((a, b) -> {
                Instant aTime = a.lastMessageAt != null ? a.lastMessageAt : Instant.EPOCH;
                Instant bTime = b.lastMessageAt != null ? b.lastMessageAt : Instant.EPOCH;
                return bTime.compareTo(aTime);
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(conversationDTOs));
    }

    @GetMapping("/messages/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<MessagePageResponseDTO>> getCustomerConversationMessages(
            @PathVariable String conversationId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        
        if (!userId.equals(conversation.getCustomer().getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied"));
        }

        // Mark messages as read for customer
        markMessagesAsRead(conversationId, userId, false);

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        
        MessagePageResponseDTO pageResponse = convertToMessagePage(messagePage);
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PostMapping("/messages")
    @Transactional
    public ResponseEntity<ApiResponse<ChatMessageResponseDTO>> sendCustomerMessage(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CreateMessageRequestDTO request
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }
        if (request.content == null || request.content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Message content is required"));
        }

        User customer = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Conversation conversation;
        if (request.conversationId != null && !request.conversationId.isBlank()) {
            conversation = conversationRepository.findById(request.conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            
            if (!userId.equals(conversation.getCustomer().getId())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied"));
            }
        } else {
            // Get or create conversation with seller
            Seller seller;
            if (request.recipientId != null && !request.recipientId.isBlank()) {
                // Try to find seller by sellerId first, then by userId
                seller = sellerRepository.findById(request.recipientId).orElse(null);
                if (seller == null) {
                    seller = sellerRepository.findByUserId(request.recipientId).orElse(null);
                }
                
                if (seller == null) {
                    return ResponseEntity.status(404)
                            .body(ApiResponse.error("Seller not found"));
                }
            } else {
                // Fallback to first seller if no recipientId provided
                seller = sellerRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No seller found"));
            }
            
            conversation = conversationRepository.findBySellerIdAndCustomerId(
                seller.getId(), userId
            ).orElse(null);
            
            if (conversation == null) {
                conversation = new Conversation();
                conversation.setId(UUID.randomUUID().toString());
                conversation.setSeller(seller);
                conversation.setCustomer(customer);
                conversation.setSellerUnreadCount(0);
                conversation = conversationRepository.save(conversation);
            }
        }

        Message message = createMessage(conversation, customer, request.content);
        message = messageRepository.save(message);

        // Update conversation last message
        conversation.setLastMessage(request.content);
        conversation.setLastMessageAt(message.getCreatedAt() != null ?
            message.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() :
            LocalDateTime.now());
        conversation.setSellerUnreadCount(conversation.getSellerUnreadCount() + 1);
        conversationRepository.save(conversation);

        ChatMessageResponseDTO dto = convertMessageToDTO(message);
        broadcastMessage(conversation, dto, customer);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /* ===================== Seller Endpoints ===================== */

    @GetMapping("/seller/messages/conversations")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<MessageConversationResponseDTO>>> getSellerConversations(
            @RequestHeader(value = "X-User-Id", required = false) String sellerUserId
    ) {
        if (sellerUserId == null || sellerUserId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Seller not authenticated"));
        }

        // Verify seller exists
        Seller seller = sellerRepository.findByUserId(sellerUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        // Try both queries: by userId and by sellerId
        List<Conversation> conversations = conversationRepository.findBySellerUserIdOrdered(sellerUserId);
        if (conversations.isEmpty()) {
            // Fallback: try by sellerId directly
            conversations = conversationRepository.findBySellerIdOrdered(seller.getId());
        }
        
        // Debug logging
        System.out.println("[DEBUG] Seller userId: " + sellerUserId);
        System.out.println("[DEBUG] Seller id: " + seller.getId());
        System.out.println("[DEBUG] Found conversations count: " + conversations.size());
        conversations.forEach(c -> {
            System.out.println("[DEBUG] Conversation id: " + c.getId() + 
                             ", seller id: " + (c.getSeller() != null ? c.getSeller().getId() : "null") +
                             ", customer id: " + (c.getCustomer() != null ? c.getCustomer().getId() : "null"));
        });
        
        List<MessageConversationResponseDTO> conversationDTOs = conversations.stream()
            .map(this::convertConversationToDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(conversationDTOs));
    }

    @GetMapping("/seller/messages/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<MessagePageResponseDTO>> getSellerConversationMessages(
            @PathVariable String conversationId,
            @RequestHeader(value = "X-User-Id", required = false) String sellerUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        if (sellerUserId == null || sellerUserId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Seller not authenticated"));
        }

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        
        if (conversation.getSeller() == null || 
            !sellerUserId.equals(conversation.getSeller().getUser().getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied"));
        }

        // Mark messages as read for seller
        markMessagesAsRead(conversationId, sellerUserId, true);

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        
        MessagePageResponseDTO pageResponse = convertToMessagePage(messagePage);
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PostMapping("/seller/messages")
    @Transactional
    public ResponseEntity<ApiResponse<ChatMessageResponseDTO>> sendSellerMessage(
            @RequestHeader(value = "X-User-Id", required = false) String sellerUserId,
            @RequestBody CreateMessageRequestDTO request
    ) {
        if (sellerUserId == null || sellerUserId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Seller not authenticated"));
        }
        if (request.content == null || request.content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Message content is required"));
        }

        Seller seller = sellerRepository.findByUserId(sellerUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        User sellerUser = seller.getUser();

        Conversation conversation;
        if (request.conversationId != null && !request.conversationId.isBlank()) {
            conversation = conversationRepository.findById(request.conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            
            if (!seller.getId().equals(conversation.getSeller().getId())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access denied"));
            }
        } else if (request.recipientId != null && !request.recipientId.isBlank()) {
            User customer = userRepository.findById(request.recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            
            conversation = conversationRepository.findBySellerIdAndCustomerId(
                seller.getId(), request.recipientId
            ).orElse(null);
            
            if (conversation == null) {
                conversation = new Conversation();
                conversation.setId(UUID.randomUUID().toString());
                conversation.setSeller(seller);
                conversation.setCustomer(customer);
                conversation.setSellerUnreadCount(0);
                conversation = conversationRepository.save(conversation);
            }
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("recipientId is required when conversationId is missing"));
        }

        Message message = createMessage(conversation, sellerUser, request.content);
        message = messageRepository.save(message);

        // Update conversation last message
        conversation.setLastMessage(request.content);
        conversation.setLastMessageAt(message.getCreatedAt() != null ?
            message.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() :
            LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageResponseDTO dto = convertMessageToDTO(message);
        broadcastMessage(conversation, dto, sellerUser);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /* ===================== Helpers ===================== */

    private Message createMessage(Conversation conversation, User sender, String content) {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        return message;
    }

    @Transactional
    private void markMessagesAsRead(String conversationId, String userId, boolean isSeller) {
        List<Message> unreadMessages = messageRepository.findTop20ByConversationIdAndReadAtIsNullOrderByCreatedAtDesc(conversationId);
        for (Message message : unreadMessages) {
            // Only mark as read if message is from the other party
            boolean isFromOtherParty = isSeller ? 
                message.getSender().getUserType() != User.UserType.SELLER :
                message.getSender().getUserType() == User.UserType.SELLER;
            
            if (isFromOtherParty && message.getReadAt() == null) {
                message.setReadAt(LocalDateTime.now());
                messageRepository.save(message);
            }
        }
        
        // Update conversation unread count
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation != null) {
            if (isSeller) {
                conversation.setSellerUnreadCount(0);
            }
            conversationRepository.save(conversation);
        }
    }

    private MessageConversationResponseDTO convertConversationToDTO(Conversation conversation) {
        try {
            MessageConversationResponseDTO dto = new MessageConversationResponseDTO();
            dto.id = conversation.getId();
            dto.customerId = conversation.getCustomer() != null ? conversation.getCustomer().getId() : null;
            dto.customerName = conversation.getCustomer() != null ? conversation.getCustomer().getFullName() : null;
            dto.customerEmail = conversation.getCustomer() != null ? conversation.getCustomer().getEmail() : null;
            dto.sellerId = conversation.getSeller() != null ? conversation.getSeller().getId() : null;
            dto.sellerName = conversation.getSeller() != null ? conversation.getSeller().getShopName() : null;
            dto.lastMessage = conversation.getLastMessage();
            dto.lastMessageAt = conversation.getLastMessageAt() != null ?
                conversation.getLastMessageAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
            dto.sellerUnreadCount = conversation.getSellerUnreadCount() != null ? conversation.getSellerUnreadCount() : 0;
            
            // Count customer unread messages
            try {
                long customerUnread = messageRepository.countUnreadForSeller(conversation.getId());
                dto.customerUnreadCount = (int) customerUnread;
            } catch (Exception e) {
                System.out.println("[WARN] Failed to count unread messages for conversation " + conversation.getId() + ": " + e.getMessage());
                dto.customerUnreadCount = 0;
            }
            
            return dto;
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to convert conversation to DTO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private ChatMessageResponseDTO convertMessageToDTO(Message message) {
        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();
        dto.id = message.getId();
        dto.conversationId = message.getConversation() != null ? message.getConversation().getId() : null;
        dto.senderId = message.getSender() != null ? message.getSender().getId() : null;
        dto.senderName = message.getSender() != null ? message.getSender().getFullName() : null;
        dto.content = message.getContent();
        dto.createdAt = message.getCreatedAt() != null ?
            message.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        return dto;
    }

    private MessagePageResponseDTO convertToMessagePage(Page<Message> messagePage) {
        MessagePageResponseDTO dto = new MessagePageResponseDTO();
        dto.content = messagePage.getContent().stream()
            .map(this::convertMessageToDTO)
            .collect(Collectors.toList());
        dto.totalElements = messagePage.getTotalElements();
        dto.totalPages = messagePage.getTotalPages();
        dto.size = messagePage.getSize();
        dto.number = messagePage.getNumber();
        return dto;
    }

    private void broadcastMessage(Conversation conversation, ChatMessageResponseDTO dto, User sender) {
        if (conversation == null || dto == null || sender == null) {
            return;
        }

        realtimeMessagingService.sendChatMessage(conversation.getId(), dto);

        User recipient = resolveRecipient(conversation, sender);
        if (recipient == null || recipient.getId() == null || recipient.getId().equals(sender.getId())) {
            return;
        }

        String link = recipient.getUserType() == User.UserType.SELLER
            ? "/seller/messages?conversationId=" + conversation.getId()
            : "/messages?conversationId=" + conversation.getId();

        notificationService.createAndDispatch(
            recipient,
            Notification.NotificationType.CHAT_MESSAGE,
            buildNotificationTitle(sender),
            truncateContent(dto.content),
            link,
            conversation.getId(),
            null
        );
    }

    private User resolveRecipient(Conversation conversation, User sender) {
        if (conversation.getSeller() != null &&
            conversation.getSeller().getUser() != null &&
            !conversation.getSeller().getUser().getId().equals(sender.getId())) {
            return conversation.getSeller().getUser();
        }
        if (conversation.getCustomer() != null &&
            !conversation.getCustomer().getId().equals(sender.getId())) {
            return conversation.getCustomer();
        }
        return null;
    }

    private String buildNotificationTitle(User sender) {
        if (sender == null) {
            return "Tin nhắn mới";
        }
        String name = sender.getFullName() != null ? sender.getFullName() : sender.getEmail();
        return name != null ? "Tin nhắn mới từ " + name : "Tin nhắn mới";
    }

    private String truncateContent(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 160 ? content.substring(0, 157) + "..." : content;
    }
}

