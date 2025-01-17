package com.gauntletai.chat.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Builder
@Document(collection = "chats")
class Chat {
    @Id
    private String id;
    private String name;
    private String description;
    private ChatType type;
    private Date lastMessageAt;
}
