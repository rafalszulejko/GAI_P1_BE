package com.gauntletai.chat.domain;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gauntletai.chat.domain.exception.EntityNotFoundException;
import com.gauntletai.chat.s3.S3Attachment;
import com.gauntletai.chat.s3.S3Service;
import com.gauntletai.chat.security.SecurityUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final SseService sseService;
    private final S3Service s3Service;


    List<Message> getChatMessages(String chatId) {
        log.debug("Fetching messages for chat {}", chatId);
        List<Message> messages = messageRepository.findByChatIdOrderBySentAtDesc(chatId);
        log.debug("Found {} messages for chat {}", messages.size(), chatId);
        return messages;
    }

    Message createMessage(Message message, boolean local) {
        Message newMessage = Message.builder()
            .id(java.util.UUID.randomUUID().toString())
            .content(message.getContent())
            .chatId(message.getChatId())
            .senderId(local ? message.getSenderId() : SecurityUtils.getCurrentUserId())
            .sentAt(new Date())
            .build();
            
        Message savedMessage = messageRepository.save(newMessage);
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

    public S3Attachment uploadFile(String messageId, MultipartFile file) {
        S3Attachment attachment = s3Service.uploadFile(file);
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException(Message.class, messageId));
        message.getAttachments().add(attachment);
        messageRepository.save(message);
        return attachment;
    }

    public ResponseEntity<byte[]> getAttachment(String messageId, String key) {
        S3Attachment attachment = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException(Message.class, messageId))
            .getAttachments().stream()
            .filter(s3Attachment -> s3Attachment.getKey().equals(key))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(S3Attachment.class, key));
        return s3Service.getAttachment(attachment);
    }
} 
