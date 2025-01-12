package com.gauntletai.chat.domain;

import com.gauntletai.chat.domain.exception.EntityNotFoundException;
import com.gauntletai.chat.security.SecurityUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Transactional
class ChatMemberService {
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRepository chatRepository;

    ChatMemberService(ChatMemberRepository chatMemberRepository,
                         ChatRepository chatRepository) {
        this.chatMemberRepository = chatMemberRepository;
        this.chatRepository = chatRepository;
    }

    List<ChatMember> getCurrentUserMemberships() {
        String userId = SecurityUtils.getCurrentUserId();
        return chatMemberRepository.findByUserId(userId);
    }

    ChatMember createMembership(ChatMember chatMember) {
        // Verify chat exists
        if (!chatRepository.existsById(chatMember.getChatId())) {
            throw new EntityNotFoundException(Chat.class, chatMember.getChatId());
        }

        // Check if membership already exists
        if (chatMemberRepository.existsByChatIdAndUserId(chatMember.getChatId(), chatMember.getUserId())) {
            throw new IllegalStateException("User is already a member of this chat");
        }

        chatMember.setId(java.util.UUID.randomUUID().toString());
        chatMember.setJoinedAt(new Date());
        return chatMemberRepository.save(chatMember);
    }

    List<ChatMember> getChatMemberships(String chatId) {
        return chatMemberRepository.findByChatId(chatId);
    }
} 