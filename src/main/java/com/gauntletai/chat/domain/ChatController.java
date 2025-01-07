package com.gauntletai.chat.domain;

import com.gauntletai.chat.config.SecurityUtils;
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
    List<Chat> getUserChats() {
        String userId = SecurityUtils.getCurrentUserId();
        return chatService.getUserChats(userId);
    }

    @PostMapping
    Chat createChat(@RequestBody Chat chat) {
        String userId = SecurityUtils.getCurrentUserId();
        return chatService.createChat(chat, userId);
    }

    @GetMapping("/{chatId}")
    Chat getChat(@PathVariable String chatId) {
        return chatService.getChatById(chatId);
    }

    // @PostMapping("/{chatId}/members")
    // ChatMember addMember(@PathVariable String chatId,
    //                     @RequestParam String userId,
    //                     @RequestParam String role) {
    //     return chatService.addMember(chatId, userId, role);
    // }
} 