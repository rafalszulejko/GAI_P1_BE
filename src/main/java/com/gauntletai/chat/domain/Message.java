package com.gauntletai.chat.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.gauntletai.chat.s3.S3Attachment;

import java.util.Date;
import java.util.List;

@Data
@Builder
@Document(collection = "messages")
class Message {
    @Id
    private String id;
    private String chatId;
    private String senderId;
    private String content;
    private Date sentAt;
    private String threadId;
    private List<S3Attachment> attachments;
}
