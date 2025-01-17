package com.gauntletai.chat.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserAvatarRepository extends MongoRepository<UserAvatar, String> {
    Optional<UserAvatar> findByRealUserId(String realUserId);
    Optional<UserAvatar> findByAvatarUserId(String avatarUserId);
    boolean existsByAvatarUserId(String avatarUserId);
} 
