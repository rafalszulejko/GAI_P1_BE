package com.gauntletai.chat.domain;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gauntletai.chat.domain.exception.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAvatarService {
    private final UserAvatarRepository userAvatarRepository;
    private final UserService userService;
    private final OpenAiChatModel openAiChatModel;
    private final SearchService searchService;
    private final MessageRepository messageRepository;

    String USER_STYLE_ANALYSIS_PROMPT = """
                Analyze the following messages to identify the key writing style characteristics. Focus only on consistent, distinctive patterns. Format your response as specific, actionable style rules. Exclude any broad or subjective observations.

                Previous messages:
                %s
            """;

    public UserAvatar createUserAvatar(String realUserId) {
        User realUser = userService.findById(realUserId);
        log.info("Creating avatar for user: {}", realUser.getUsername());

        User avatarUser = userService.createNewUser(realUser.getUsername(), "", "", true);
        log.info("Avatar user: {}: {}", avatarUser.getId(), avatarUser.getUsername());

        List<Message> messages = messageRepository.findFirst50BySenderIdOrderBySentAtDesc(realUserId);

        UserStyles userStyles = ChatClient.create(openAiChatModel).prompt()
                .user(u -> u.text(userAnalysisPrompt(messages))).call().entity(UserStyles.class);

        log.info("User styles: {}", userStyles);

        UserAvatar avatar = UserAvatar.builder()
                .id(UUID.randomUUID().toString())
                .realUserId(realUserId)
                .avatarUserId(avatarUser.getId())
                .styles(userStyles)
                .build();

        return userAvatarRepository.save(avatar);
    }

    private String userAnalysisPrompt(List<Message> messages) {
        return String.format(USER_STYLE_ANALYSIS_PROMPT,
                messages.stream().map(Message::getContent).collect(Collectors.joining("\n")));
    }

    public String generateAvatarResponse(Message currentMessage, List<Message> conversationContext, User avatarUser, UserAvatar avatar) {
        User humanUser = userService.findById(currentMessage.getSenderId());

        String avatarResponsePrompt = avatarResponsePrompt(humanUser, avatarUser, conversationContext, currentMessage,
                avatar);
        log.info("Avatar response prompt: {}", avatarResponsePrompt);
        return openAiChatModel.call(new Prompt(avatarResponsePrompt)).getResult().getOutput().getText();
    }

    public UserAvatar findByAvatarUserId(String id) {
        return userAvatarRepository.findByAvatarUserId(id)
                .orElseThrow(() -> new EntityNotFoundException(UserAvatar.class, id));
    }

    private String avatarResponsePrompt(User humanUser, User avatarUser, List<Message> conversationContext,
            Message currentMessage, UserAvatar avatar) {
        Map<String, String> userNames = Map.of(
                humanUser.getId(), humanUser.getUsername(),
                avatarUser.getId(), avatarUser.getUsername());

        String conversationContextString = conversationContext.stream()
                .map(message -> userNames.get(message.getSenderId()) + ": " + message.getContent())
                .collect(Collectors.joining("\n"));

        String currentMessageString = humanUser.getUsername() + ": " + currentMessage.getContent();
        String avatarStyleString = avatar.getStyles().toString();

        String previousUserMessages = searchService
                .vectorSearchByUser(currentMessage.getContent(), avatar.getRealUserId()).stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        return String.format("""
                    You are an AI avatar of %s. You are currently in a chat with %s.
                    Here are the previous messages in the conversation:
                    %s
                    You are given the following message:
                    %s
                    You are given the following style rules:
                    %s
                    Here are your other messages from other chats related to the message you are responding to:
                    %s
                    Considering all given information, return a response to the user in the style of the avatar.
                """, avatarUser.getUsername(), humanUser.getUsername(),
                conversationContextString, currentMessageString, avatarStyleString, previousUserMessages);
    }

    public boolean isAvatarUser(String userId) {
        return userAvatarRepository.existsByAvatarUserId(userId);
    }
}