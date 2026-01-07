package com.harsha.taskpilot.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class TaskPlanner {
    private final ChatClient chatClient;

    public TaskPlanner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String plan(String userGoal){
        String systemPrompt = """
                You are a task planning agent.
                Given a user goal, break it into clear, ordered steps.
                Respond with a numbered list.
                Do NOT execute steps.
                """;

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userGoal)
                .call()
                .content();
    }
}
