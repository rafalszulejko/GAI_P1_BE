package com.gauntletai.chat.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gauntletai.chat.config.SecurityUtils;
import com.gauntletai.chat.domain.exception.EntityNotFoundException;
import java.util.Date;
import java.util.List;

@Service
@Transactional
class ChatService {
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;

    ChatService(ChatRepository chatRepository, 
               ChatMemberRepository chatMemberRepository,
               UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.userRepository = userRepository;
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
} 