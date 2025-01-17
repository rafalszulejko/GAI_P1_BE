package com.gauntletai.chat.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gauntletai.chat.domain.dto.CreateChatCommand;
import com.gauntletai.chat.domain.exception.EntityNotFoundException;
import com.gauntletai.chat.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class ChatService {
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserAvatarService userAvatarService;

    List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    Chat createChat(CreateChatCommand command) {
        String chatId = java.util.UUID.randomUUID().toString();
        Chat chat = Chat.builder()
            .id(chatId)
            .name(command.getName())
            .description(command.getDescription())
            .type(command.getType())
            .build();
        Chat savedChat = chatRepository.save(chat);
        String creatorId = SecurityUtils.getCurrentUserId();
        
        // Add creator as a member
        ChatMember creator = ChatMember.builder()
                .id(java.util.UUID.randomUUID().toString())
                .chatId(savedChat.getId())
                .userId(creatorId)
                .joinedAt(new Date())
                .build();
        chatMemberRepository.save(creator);

        if (chat.getType() == ChatType.AI) {
            UserAvatar avatar = userAvatarService.createUserAvatar(command.getMembers().get(0));
            chatMemberRepository.save(ChatMember.builder()
                .id(java.util.UUID.randomUUID().toString())
                .chatId(savedChat.getId())
                .userId(avatar.getAvatarUserId())
                .joinedAt(new Date())
                .build());
        }
        
        return savedChat;
    }

    Chat getChatById(String chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException(Chat.class, chatId));
    }

    Chat updateChat(String chatId, Chat chatUpdate) {
        if (!chatId.equals(chatUpdate.getId())) {
            throw new IllegalArgumentException("Chat ID in path and request body must match");
        }

        Chat existingChat = getChatById(chatId);
        
        // Only update allowed fields
        existingChat.setName(chatUpdate.getName());
        existingChat.setDescription(chatUpdate.getDescription());
        
        return chatRepository.save(existingChat);
    }

    public String getOtherUser(String chatId) {
        Chat chat = getChatById(chatId);
        String senderId = SecurityUtils.getCurrentUserId();
        return getOtherUser(chat, senderId);
    }

    public String getOtherUser(Chat chat, String senderId) {
        log.info("Getting other user for chat: {}:{} that is not {}", chat.getId(), chat.getName(), senderId);
        if (chat.getType().equals(ChatType.DIRECT) || chat.getType().equals(ChatType.AI)) {
            String foundId =  chatMemberRepository.findByChatId(chat.getId()).stream()
                    .filter(cm -> !cm.getUserId().equals(senderId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(ChatMember.class, chat.getId()))
                .getUserId();
            log.info("Found other user: {}", foundId);
            return foundId;
        }
        throw new IllegalArgumentException("Chat is not direct");
    }
}