package com.gauntletai.chat.domain;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import com.gauntletai.chat.domain.exception.EntityNotFoundException;
import com.gauntletai.chat.s3.S3Attachment;
import com.gauntletai.chat.s3.S3Service;
import com.gauntletai.chat.security.SecurityUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final SseService sseService;
    private final S3Service s3Service;
    private final VectorStore vectorStore;
    private final ChatService chatService;
    private final UserAvatarService userAvatarService;

    @Value("${ai.avatar.max-context-messages:50}")
    private int maxContextMessages;


    Message getMessage(String messageId) {
        return messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException(Message.class, messageId));
    }

    Page<Message> getChatMessages(String chatId, Pageable pageable) {
        log.debug("Fetching messages for chat {} with pagination", chatId);
        Page<Message> messages = messageRepository.findByChatIdOrderBySentAtDesc(chatId, pageable);
        log.debug("Found {} messages for chat {}", messages.getTotalElements(), chatId);
        return messages;
    }

    Message createMessage(Message message, boolean local, boolean autoGenerated) {
        Message newMessage = Message.builder()
            .id(java.util.UUID.randomUUID().toString())
            .content(message.getContent())
            .chatId(message.getChatId())
            .senderId(local ? message.getSenderId() : SecurityUtils.getCurrentUserId())
            .sentAt(new Date())
            .build();
            
        Message savedMessage = messageRepository.save(newMessage);
        sseService.broadcastToChat(savedMessage.getChatId(), "NEW_MESSAGE", savedMessage);
        saveInVectorStore(savedMessage);

        if (!autoGenerated) {
            Chat chat = chatService.getChatById(savedMessage.getChatId());
            if (chat.getType() == ChatType.AI && !userAvatarService.isAvatarUser(savedMessage.getSenderId())) {
                createAvatarReply(chat, savedMessage);
            }
        }

        return savedMessage;
    }

    private void createAvatarReply(Chat chat, Message userMessage) {
        List<Message> conversationContext = getChatMessages(chat.getId(), PageRequest.of(0, maxContextMessages)).getContent();
        User otherUser = userService.findById(chatService.getOtherUser(chat, userMessage.getSenderId()));
        log.info("Other user: {}", otherUser.getUsername());
        UserAvatar avatar = userAvatarService.findByAvatarUserId(otherUser.getId());
        log.info("Avatar found: {}", avatar.getId());
        String response = userAvatarService.generateAvatarResponse(userMessage, conversationContext, otherUser, avatar);
        log.info("Response: {}", response);
        createMessage(Message.builder()
            .content(response)
            .chatId(chat.getId())
            .senderId(avatar.getAvatarUserId())
            .sentAt(new Date())
            .build(), true, true);
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

    private void saveInVectorStore(Message message) {
        Document doc = Document.builder()
                .id(message.getId())
                .text(message.getContent())
                .metadata(Map.of(
                        "messageId", message.getId(),
                        "senderId", message.getSenderId(),
                        "chatId", message.getChatId()))
                .build();
        vectorStore.add(List.of(doc));
    }
} 
