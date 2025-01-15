package com.gauntletai.chat.domain.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiSearchResult {
    private String summary;
    private List<String> messages;
}
