package com.gauntletai.chat.domain;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/local")
@RequiredArgsConstructor
class LocalController {
    private final ChatService chatService;
    private final ChatMemberService chatMemberService;
    private final MessageService messageService;

    @PostMapping("/chat")
    Chat createChat(@RequestBody Chat chat) {
        return chatService.createChat(chat);
    }

    @PostMapping("/chatmember")
    ChatMember createChatMember(@RequestBody ChatMember chatMember) {
        return chatMemberService.createMembership(chatMember);
    }

    @PostMapping("/message")
    Message createMessage(@RequestBody Message message) {
        return messageService.createMessage(message, true);
    }
} 