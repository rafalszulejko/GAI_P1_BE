package com.gauntletai.chat.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gauntletai.chat.domain.exception.EntityNotFoundException;

import java.util.Date;
import java.util.List;

@Service
@Transactional
class MessageService {
    private final MessageRepository messageRepository;

    MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    List<Message> getChatMessages(String chatId) {
        return messageRepository.findByChatIdOrderBySentAtDesc(chatId);
    }

    Message createMessage(Message message, String chatId, String userId) {
        Message newMessage = Message.builder()
            .id(java.util.UUID.randomUUID().toString())
            .content(message.getContent())
            .chatId(chatId)
            .senderId(userId)
            .sentAt(new Date())
            .build();
        return messageRepository.save(newMessage);
    }

    Message updateMessage(String messageId, String content) {
        return messageRepository.findById(messageId)
            .map(message -> {
                message.setContent(content);
                return messageRepository.save(message);
            })
            .orElseThrow(() -> new EntityNotFoundException(Message.class, messageId));
    }

} 