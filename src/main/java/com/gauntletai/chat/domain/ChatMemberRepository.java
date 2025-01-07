package com.gauntletai.chat.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
interface ChatMemberRepository extends MongoRepository<ChatMember, String> {
    List<ChatMember> findByChatId(String chatId);
    List<ChatMember> findByUserId(String userId);
    Optional<ChatMember> findByChatIdAndUserId(String chatId, String userId);
    boolean existsByChatIdAndUserId(String chatId, String userId);
} 