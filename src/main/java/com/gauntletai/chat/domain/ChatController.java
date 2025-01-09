package com.gauntletai.chat.domain;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
class ChatController {
    private final ChatService chatService;

    ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    List<Chat> getAllChats() {
        return chatService.getAllChats();
    }

    @PostMapping
    Chat createChat(@RequestBody Chat chat) {
        return chatService.createChat(chat);
    }

    @GetMapping("/{chatId}")
    Chat getChat(@PathVariable String chatId) {
        return chatService.getChatById(chatId);
    }

    @PutMapping("/{chatId}")
    Chat updateChat(@PathVariable String chatId, @RequestBody Chat chat) {
        return chatService.updateChat(chatId, chat);
    }
} 