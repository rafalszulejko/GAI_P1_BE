package com.gauntletai.chat.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByType(String type);
} 