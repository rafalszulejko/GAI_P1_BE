package com.gauntletai.chat.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByChatIdOrderBySentAtDesc(String chatId, Pageable pageable);
    List<Message> findByChatIdAndSentAtGreaterThan(String chatId, Date date);
    List<Message> findBySenderIdAndChatId(String senderId, String chatId);
    List<Message> findByChatIdOrderBySentAtDesc(String chatId);
} 