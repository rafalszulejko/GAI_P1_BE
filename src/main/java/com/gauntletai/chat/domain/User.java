package com.gauntletai.chat.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.Date;
import java.util.UUID;

@Data
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

    public static User createFromAuth0(String auth0Id, String email, String name) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setAuth0Id(auth0Id);
        user.setEmail(email);
        user.setUsername(name);
        user.setCreatedAt(new Date());
        user.setLastActive(new Date());
        user.setOnline(true);
        return user;
    }
}
