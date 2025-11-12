package com.shopcuathuy.service;

import com.shopcuathuy.dto.ConversationDTO;
import com.shopcuathuy.dto.CreateMessageRequest;
import com.shopcuathuy.dto.MessageDTO;
import com.shopcuathuy.entity.Conversation;
import com.shopcuathuy.entity.Message;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.BadRequestException;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ConversationRepository;
import com.shopcuathuy.repository.MessageRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    public List<ConversationDTO> getSellerConversations(String sellerUserId) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        return conversationRepository.findBySellerUserIdOrdered(sellerUserId)
                .stream()
                .map(this::toConversationDTO)
                .collect(Collectors.toList());
    }

    public Page<MessageDTO> getConversationMessages(String sellerUserId, String conversationId, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getSeller().getUser().getId().equals(sellerUserId)) {
            throw new BadRequestException("Conversation does not belong to seller");
        }

        Page<Message> page = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        // mark unread messages as read for seller
        messageRepository.findTop20ByConversationIdAndReadAtIsNullOrderByCreatedAtDesc(conversationId)
                .stream()
                .filter(message -> !message.getSender().getId().equals(sellerUserId))
                .forEach(message -> {
                    message.setReadAt(LocalDateTime.now());
                    messageRepository.save(message);
                });

        conversation.setSellerUnreadCount((int) messageRepository.countUnreadForSeller(conversationId));

        List<MessageDTO> dtoList = page.getContent().stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    public MessageDTO sendMessageAsSeller(String sellerUserId, CreateMessageRequest request) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        User customer = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getId().equals(sellerUserId)) {
            throw new BadRequestException("Cannot send message to yourself");
        }

        Conversation conversation = conversationRepository
                .findBySellerIdAndCustomerId(seller.getId(), customer.getId())
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setSeller(seller);
                    c.setCustomer(customer);
                    return conversationRepository.save(c);
                });

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(seller.getUser());
        message.setContent(request.getContent());
        message.setAttachments(request.getAttachments());
        message.setReadAt(null);

        Message saved = messageRepository.save(message);

        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageAt(saved.getCreatedAt());
        conversation.setSellerUnreadCount(0);
        conversationRepository.save(conversation);

        return toMessageDTO(saved);
    }

    private ConversationDTO toConversationDTO(Conversation conversation) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setCustomerId(conversation.getCustomer().getId());
        dto.setCustomerName(conversation.getCustomer().getFullName());
        dto.setCustomerEmail(conversation.getCustomer().getEmail());
        dto.setLastMessage(conversation.getLastMessage());
        dto.setLastMessageAt(conversation.getLastMessageAt());
        dto.setSellerUnreadCount(conversation.getSellerUnreadCount());
        return dto;
    }

    private MessageDTO toMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setContent(message.getContent());
        dto.setAttachments(message.getAttachments());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setReadAt(message.getReadAt());
        return dto;
    }
}
