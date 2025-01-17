package com.gauntletai.chat.domain;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserStyles {
    private String sentenceStructure;
    private String formatting;
    private String vocabulary;
    private String responseLength;

    public String toString() {
        return """
                Sentence Structure: %s
                Formatting: %s
                Vocabulary: %s
                Response Length: %s""".formatted(sentenceStructure, formatting, vocabulary, responseLength);
    }

    public static String openAiStructuredOutputJsonSchema = """
            {
                "sentenceStructure": { "type": "string" },
                "formatting": { "type": "string" },
                "vocabulary": { "type": "string" },
                "responseLength": { "type": "string" }
            }
            """;
}
