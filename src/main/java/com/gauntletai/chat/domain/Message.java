package com.gauntletai.chat.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.gauntletai.chat.s3.S3Attachment;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    private String chatId;
    private String senderId;
    private String content;
    private Date sentAt;
    private String threadId;
    private List<S3Attachment> attachments;
    
    public Map<String, Object> toMetadata() {
        return Map.of("messageId", id, "userId", senderId, "chatId", chatId);
    }
}
