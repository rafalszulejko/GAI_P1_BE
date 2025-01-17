package com.gauntletai.chat.domain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gauntletai.chat.s3.S3Attachment;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
class MessageController {
    private final MessageService messageService;

    @GetMapping("/chat/{chatId}")
    Page<Message> getChatMessages(@PathVariable String chatId, Pageable pageable) {
        return messageService.getChatMessages(chatId, pageable);
    }

    @PostMapping
    Message createMessage(@RequestBody Message message) {
        return messageService.createMessage(message, false, false);
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