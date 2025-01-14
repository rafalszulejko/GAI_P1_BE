package com.gauntletai.chat.domain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gauntletai.chat.s3.S3Attachment;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
class MessageController {
    private final MessageService messageService;

    @GetMapping("/chat/{chatId}")
    List<Message> getChatMessages(@PathVariable String chatId) {
        return messageService.getChatMessages(chatId);
    }

    @PostMapping
    Message createMessage(@RequestBody Message message) {
        return messageService.createMessage(message, false);
    }

    @PostMapping("/{messageId}/attachments")
    S3Attachment uploadAttachment(@PathVariable String messageId,
                                @RequestPart("file") MultipartFile file) {
        return messageService.uploadFile(messageId, file);
    }

    @GetMapping("/{messageId}/attachments/{key}")
    ResponseEntity<byte[]> getAttachment(@PathVariable String messageId,
                                        @PathVariable String key) {
        return messageService.getAttachment(messageId, key);
    }

    @PutMapping("/{messageId}")
    Message updateMessage(@PathVariable String messageId,
                        @RequestBody Message message) {
        return messageService.updateMessage(messageId, message);
    }
} 