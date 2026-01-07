package com.harsha.taskpilot.tools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class TextAnalysisTool implements Tool {
    private final ChatClient chatClient;

    public TextAnalysisTool(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String name() {
        return "TextAnalysisTool";
    }

    @Override
    public String execute(String input) {
        String systemPrompt = """
                You are a senior Java engineer.
                Analyze the given text or code and provide clear suggestions.
                """;

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(input)
                .call()
                .content();
    }
}
