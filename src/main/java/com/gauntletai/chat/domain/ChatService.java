package com.gauntletai.chat.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gauntletai.chat.domain.exception.EntityNotFoundException;
import com.gauntletai.chat.security.SecurityUtils;

import java.util.Date;
import java.util.List;

@Service
@Transactional
class ChatService {
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;

    ChatService(ChatRepository chatRepository, 
               ChatMemberRepository chatMemberRepository) {
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
    }

    List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    Chat createChat(Chat chat) {
        String chatId = java.util.UUID.randomUUID().toString();
        chat.setId(chatId);
        Chat savedChat = chatRepository.save(chat);
        
        // Add creator as a member
        ChatMember creator = ChatMember.builder()
                .id(java.util.UUID.randomUUID().toString())
                .chatId(savedChat.getId())
                .userId(SecurityUtils.getCurrentUserId())
                .joinedAt(new Date())
                .build();
        chatMemberRepository.save(creator);
        
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
} 