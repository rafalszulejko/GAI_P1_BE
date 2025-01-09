package com.gauntletai.chat.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gauntletai.chat.config.SecurityUtils;
import com.gauntletai.chat.domain.exception.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
class MessageService {
    private final MessageRepository messageRepository;

    MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    List<Message> getChatMessages(String chatId) {
        return messageRepository.findByChatIdOrderBySentAtDesc(chatId);
    }

    Message createMessage(Message message) {
        Message newMessage = Message.builder()
            .id(java.util.UUID.randomUUID().toString())
            .content(message.getContent())
            .chatId(message.getChatId())
            .senderId(SecurityUtils.getCurrentUserId())
            .sentAt(new Date())
            .build();
        return messageRepository.save(newMessage);
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

} 