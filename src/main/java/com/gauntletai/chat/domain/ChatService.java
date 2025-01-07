package com.gauntletai.chat.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gauntletai.chat.domain.exception.EntityNotFoundException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    List<Chat> getUserChats(String userId) {
        return chatRepository.findAllById(
                chatMemberRepository.findByUserId(userId).stream()
                        .map(ChatMember::getChatId)
                        .toList());
    }

    Chat createChat(Chat chat, String creatorId) {
        String chatId = java.util.UUID.randomUUID().toString();
        chat.setId(chatId);
        Chat savedChat = chatRepository.save(chat);
        
        // Add creator as a member
        ChatMember creator = ChatMember.builder()
                .id(java.util.UUID.randomUUID().toString())
                .chatId(savedChat.getId())
                .userId(creatorId)
                .joinedAt(new Date())
                .build();
        chatMemberRepository.save(creator);
        
        return savedChat;
    }

    Chat getChatById(String chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException(Chat.class, chatId));
    }

    ChatMember addMember(String chatId, String userId) {        
        ChatMember member = ChatMember.builder()
                .chatId(chatId)
                .userId(userId)
                .joinedAt(new Date())
                .build();
        
        return chatMemberRepository.save(member);
    }
} 