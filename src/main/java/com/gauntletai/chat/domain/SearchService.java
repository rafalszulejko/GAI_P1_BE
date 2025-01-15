package com.gauntletai.chat.domain;

import com.gauntletai.chat.domain.dto.AiSearchResult;
import com.gauntletai.chat.domain.dto.SearchQuery;
import com.gauntletai.chat.domain.dto.SearchResults;
import com.gauntletai.chat.domain.dto.SearchType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final MessageService messageService;
    private final UserService userService;
    private final VectorStore vectorStore;
    private final OpenAiChatModel openAiChatModel;
    private final ChatService chatService;
    public SearchResults search(SearchQuery query) {
        return SearchResults.builder()
                .messages(query.getSearchTypes().contains(SearchType.MESSAGE)
                        ? messageService.search(query.getQueryString())
                        : null)
                .users(query.getSearchTypes().contains(SearchType.USER) ? userService.search(query.getQueryString())
                        : null)
                .ai(query.getSearchTypes().contains(SearchType.AI) ? prepareAiSearch(query) : null)
                .build();
    }

    private AiSearchResult prepareAiSearch(SearchQuery query) {
        List<Document> documents = vectorSearch(query.getQueryString());
        return AiSearchResult.builder()
                .summary(summarizeSearch(documents))
                .messages(documents.stream()
                        .map(document -> document.getText())
                        .collect(Collectors.toList()))
                .build();
    }

    private List<Document> vectorSearch(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(0.5)
                        .build());
    }

    private List<Document> vectorSearchInChat(String query, String chatId) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(0.5)
                        .filterExpression("chatId == '" + chatId + "'")
                        .build());
    }

    private List<Document> vectorSearchInUser(String query, String userId) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(0.5)
                        .filterExpression("userId == '" + userId + "'")
                        .build());
    }

    private String summarizeSearch(List<Document> messages) {
        StringBuilder prompt = new StringBuilder("""
                You are a helpful assistant that summarizes messages fetched from various chats.
                The following messaged are grouped by chat name.
                Summarize the context of discussion from each chat, noting user's opinions and sentiments.
                Make sure to include the chat names in the summary.
                Here are the chats:

                """);
        Map<String, String> userNames = new HashMap<>();
        Map<String, List<Document>> messagesByChat = messages.stream()
                .collect(Collectors.groupingBy(document -> (String) document.getMetadata().get("chatId")));

        messagesByChat.forEach((chatId, chatMessages) -> {
            prompt.append("Chat: ").append(chatService.getChatById(chatId).getName()).append("\n");
            chatMessages.forEach(message -> {
                String username = userNames.computeIfAbsent((String) message.getMetadata().get("senderId"),
                        userId -> userService.findById(userId)
                                .map(User::getUsername)
                                .orElse("Unknown User"));
                prompt.append(String.format("%s: %s\n", username, message.getText()));
            });
            prompt.append("\n\n");
        });

        return openAiChatModel
                .call(new Prompt(prompt.toString()))
                .getResult().getOutput().getText();
    }

}
