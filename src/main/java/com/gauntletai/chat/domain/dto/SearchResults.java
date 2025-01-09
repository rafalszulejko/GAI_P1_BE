package com.gauntletai.chat.domain.dto;

import java.util.List;

import com.gauntletai.chat.domain.MessageSearchResult;
import com.gauntletai.chat.domain.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResults {
    private List<MessageSearchResult> messages;
    private List<User> users;
}
