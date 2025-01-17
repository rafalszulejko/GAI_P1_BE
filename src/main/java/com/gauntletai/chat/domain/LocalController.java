package com.gauntletai.chat.domain;

import org.springframework.web.bind.annotation.*;

import com.gauntletai.chat.domain.dto.CreateChatCommand;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/local")
@RequiredArgsConstructor
class LocalController {
    private final ChatService chatService;
    private final ChatMemberService chatMemberService;
    private final MessageService messageService;

    @PostMapping("/chat")
    Chat createChat(@RequestBody CreateChatCommand command) {
        return chatService.createChat(command);
    }

    @PostMapping("/chatmember")
    ChatMember createChatMember(@RequestBody ChatMember chatMember) {
        return chatMemberService.createMembership(chatMember);
    }

    @PostMapping("/message")
    Message createMessage(@RequestBody Message message) {
        return messageService.createMessage(message, true, false);
    }
} 