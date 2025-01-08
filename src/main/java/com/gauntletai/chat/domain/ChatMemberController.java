package com.gauntletai.chat.domain;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat_members")
class ChatMemberController {
    private final ChatMemberService chatMemberService;

    ChatMemberController(ChatMemberService chatMemberService) {
        this.chatMemberService = chatMemberService;
    }

    @GetMapping
    List<ChatMember> getCurrentUserMemberships() {
        return chatMemberService.getCurrentUserMemberships();
    }

    @GetMapping("/{chatId}")
    List<ChatMember> getChannelMemberships(@PathVariable String chatId) {
        return chatMemberService.getChatMemberships(chatId);
    }

    @PostMapping
    ChatMember createMembership(@RequestBody ChatMember chatMember) {
        return chatMemberService.createMembership(chatMember);
    }
} 