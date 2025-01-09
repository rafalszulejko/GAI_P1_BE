package com.gauntletai.chat.domain;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
class MessageController {
    private final MessageService messageService;

    MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/chat/{chatId}")
    List<Message> getChatMessages(@PathVariable String chatId) {
        return messageService.getChatMessages(chatId);
    }

    @PostMapping
    Message createMessage(@RequestBody Message message) {
        return messageService.createMessage(message);
    }

    @PutMapping("/{messageId}")
    Message updateMessage(@PathVariable String messageId,
                        @RequestBody Message message) {
        return messageService.updateMessage(messageId, message);
    }
} 