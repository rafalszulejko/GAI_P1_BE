package com.gauntletai.chat.domain;

import com.gauntletai.chat.config.SecurityUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
class MessageController {
    private final MessageService messageService;

    MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/chats/{chatId}/messages")
    List<Message> getChatMessages(@PathVariable String chatId) {
        return messageService.getChatMessages(chatId);
    }

    @PostMapping("/chats/{chatId}/messages")
    Message createMessage(@PathVariable String chatId,
                        @RequestBody Message message) {
        String userId = SecurityUtils.getCurrentUserId();
        return messageService.createMessage(message, chatId, userId);
    }

    @PutMapping("/messages/{messageId}")
    Message updateMessage(@PathVariable String messageId,
                        @RequestBody String content) {
        return messageService.updateMessage(messageId, content);
    }
} 