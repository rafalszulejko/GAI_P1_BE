package com.gauntletai.chat.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class SearchQuery {
    private String queryString;
    private List<SearchType> searchTypes;
}
