package com.gauntletai.chat.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageSearchResult {
    private Message message;
    private User user;
}
