package com.gauntletai.chat.domain;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gauntletai.chat.domain.dto.SearchQuery;
import com.gauntletai.chat.domain.dto.SearchResults;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    @ResponseBody
    public SearchResults search(@RequestBody SearchQuery query) {
        return searchService.search(query);
    }
}
