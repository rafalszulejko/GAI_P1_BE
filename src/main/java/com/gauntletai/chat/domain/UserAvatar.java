package com.gauntletai.chat.domain;

import lombok.Data;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "user_avatars")
public class UserAvatar {
    @Id
    private String id;
    @Indexed(unique = true)
    private String realUserId;
    private String avatarUserId;
    private UserStyles styles;
} 