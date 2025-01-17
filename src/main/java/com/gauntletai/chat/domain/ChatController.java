package com.gauntletai.chat.domain;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.gauntletai.chat.domain.dto.CreateChatCommand;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
class ChatController {
    private final ChatService chatService;
    private final SseService sseService;

    ChatController(ChatService chatService, SseService sseService) {
        this.chatService = chatService;
        this.sseService = sseService;
    }

    @GetMapping
    List<Chat> getAllChats() {
        return chatService.getAllChats();
    }

    @PostMapping
    Chat createChat(@RequestBody CreateChatCommand command) {
        return chatService.createChat(command);
    }

    @GetMapping("/{chatId}")
    Chat getChat(@PathVariable String chatId) {
        return chatService.getChatById(chatId);
    }

    @GetMapping("/{chatId}/otheruser")
    String getOtherUser(@PathVariable String chatId) {
        return chatService.getOtherUser(chatId);
    }

    @PutMapping("/{chatId}")
    Chat updateChat(@PathVariable String chatId, @RequestBody Chat chat) {
        return chatService.updateChat(chatId, chat);
    }

    @GetMapping("/{chatId}/subscribe")
    SseEmitter subscribeToChat(@PathVariable String chatId) {
        return sseService.subscribeToChat(chatId);
    }
} 