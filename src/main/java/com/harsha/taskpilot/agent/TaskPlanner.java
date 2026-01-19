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
                Your job is to classify the USER GOAL.
            
                  A question is KNOWLEDGE_BASED if it involves:
                  - Java
                  - programming
                  - code
                  - best practices
                  - design
                  - software engineering
                  - rules
                  - standards
                  - architecture
            
                  If it is KNOWLEDGE_BASED → RagSearchTool MUST be used.
                  If it is NOT knowledge-based (jokes, greetings, casual chat) → RagSearchTool must NOT be used.
           
                  You MUST respond ONLY in this exact format:
           
                  IS_KNOWLEDGE_BASED: <YES or NO>
                  QUERY: <what should be searched or NONE>
                  PLAN: <short plan>
           
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
