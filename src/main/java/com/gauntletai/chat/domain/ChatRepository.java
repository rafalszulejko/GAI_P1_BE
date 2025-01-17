package com.gauntletai.chat.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByType(String type);
    Optional<String> findNameById(String id);
} 