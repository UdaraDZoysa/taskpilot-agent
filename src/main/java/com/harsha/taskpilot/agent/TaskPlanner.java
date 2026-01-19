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
                    You are TaskPilot, an autonomous AI agent.
                    
                    Your task is to decide WHICH TOOLS are needed and in WHAT ORDER.
                    
                    Available tools:
                    - RagSearchTool → retrieves internal knowledge
                    - TextAnalysisTool → performs reasoning and analysis
                    - SelfReview → verifies and improves the answer
                    
                    Rules:
                    - For Java, programming, best practices, or code → RagSearchTool MUST be included.
                    - TextAnalysisTool is ALWAYS required.
                    
                    Respond ONLY in this format:
                    
                    TOOLS:
                    - <tool name>
                    - <tool name>
                    
                    PLAN:
                    <short plan>
                    
                    USER GOAL:
                    %s
                    """.formatted(userGoal);

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userGoal)
                .call()
                .content();
    }
}
