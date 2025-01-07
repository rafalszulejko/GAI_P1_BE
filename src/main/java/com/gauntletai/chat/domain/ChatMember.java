package com.gauntletai.chat.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Builder
@Document(collection = "chat_members")
class ChatMember {
    @Id
    private String id;
    private String chatId;
    private String userId;
    private Date joinedAt;
    private Date lastRead;
    private boolean isMuted;
    private boolean isBlocked;
}
