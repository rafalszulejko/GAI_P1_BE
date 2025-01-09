package com.gauntletai.chat.domain;

import com.gauntletai.chat.domain.dto.SearchQuery;
import com.gauntletai.chat.domain.dto.SearchResults;
import com.gauntletai.chat.domain.dto.SearchType;

import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private final MessageService messageService;
    private final UserService userService;

    public SearchService(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    public SearchResults search(SearchQuery query) {
        return SearchResults.builder()
            .messages(query.getSearchTypes().contains(SearchType.MESSAGE) ? messageService.search(query.getQueryString()) : null)
            .users(query.getSearchTypes().contains(SearchType.USER) ? userService.search(query.getQueryString()) : null)
            .build();
    }

}
