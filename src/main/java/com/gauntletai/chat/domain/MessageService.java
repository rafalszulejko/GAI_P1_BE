package com.gauntletai.chat.domain;

import org.springframework.stereotype.Service;

import com.gauntletai.chat.config.SecurityUtils;
import com.gauntletai.chat.domain.exception.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final SseService sseService;

    MessageService(MessageRepository messageRepository, UserService userService, SseService sseService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.sseService = sseService;
    }

    List<Message> getChatMessages(String chatId) {
        log.debug("Fetching messages for chat {}", chatId);
        List<Message> messages = messageRepository.findByChatIdOrderBySentAtDesc(chatId);
        log.debug("Found {} messages for chat {}", messages.size(), chatId);
        return messages;
    }

    Message createMessage(Message message) {
        log.debug("Creating new message in chat {}", message.getChatId());
        Message newMessage = Message.builder()
            .id(java.util.UUID.randomUUID().toString())
            .content(message.getContent())
            .chatId(message.getChatId())
            .senderId(SecurityUtils.getCurrentUserId())
            .sentAt(new Date())
            .build();
        Message savedMessage = messageRepository.save(newMessage);
        log.debug("Broadcasting new message {} to chat {}", savedMessage.getId(), savedMessage.getChatId());
        sseService.broadcastToChat(savedMessage.getChatId(), "NEW_MESSAGE", savedMessage);
        return savedMessage;
    }

    Message updateMessage(String messageId, Message updatedMessage) {
        log.info("Updating message with id {}", messageId);
        return messageRepository.findById(messageId)
            .map(message -> {
                log.info("Updating message with id {}", messageId);
                message.setContent(updatedMessage.getContent());
                if (updatedMessage.getThreadId() != null && message.getThreadId() == null) {
                    message.setThreadId(updatedMessage.getThreadId());
                }
                return messageRepository.save(message);
            })
            .orElseThrow(() -> new EntityNotFoundException(Message.class, messageId));
    }

    public List<MessageSearchResult> search(String searchTerm) {
        return messageRepository.findByContentContainingIgnoreCase(searchTerm).stream()
            .map(message -> MessageSearchResult.builder()
                .message(message)
                .user(userService.findById(message.getSenderId())
                    .orElseThrow(() -> new EntityNotFoundException(User.class, message.getSenderId())))
                .build())
            .collect(Collectors.toList());
    }
} 
