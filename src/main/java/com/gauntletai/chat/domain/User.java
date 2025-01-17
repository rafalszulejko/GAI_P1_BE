package com.gauntletai.chat.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Builder;

import org.springframework.data.mongodb.core.index.Indexed;
import java.util.Date;

@Data
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String auth0Id;
    private String username;
    private String email;
    private String avatarUrl;
    private Date createdAt;
    private Date lastActive;
    private boolean isOnline;
    private boolean isAi;
}
