package com.gauntletai.chat.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private String avatarUrl;
    private Date createdAt;
    private Date lastActive;
    private boolean isOnline;

    public static User createFromAuth0(String auth0Id, String email, String name) {
        User user = new User();
        user.setId(auth0Id);
        user.setEmail(email);
        user.setUsername(name);
        user.setCreatedAt(new Date());
        user.setLastActive(new Date());
        user.setOnline(true);
        return user;
    }
}
