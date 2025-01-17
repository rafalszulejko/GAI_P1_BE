package com.gauntletai.chat.domain.dto;

import java.util.List;

import com.gauntletai.chat.domain.ChatType;

import lombok.Data;


@Data
public class CreateChatCommand {
    private String name;
    private String description;
    private ChatType type;
    private List<String> members;
}
