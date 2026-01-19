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
                
                STEP 1 — CLASSIFY USER INTENT.
                
                Decide whether the USER GOAL is KNOWLEDGE-BASED.
                
                A request is KNOWLEDGE-BASED ONLY if it involves:
                - Java
                - programming
                - code
                - software engineering
                - design patterns
                - best practices
                - architecture
                - technical rules or standards
                
                Casual conversation, jokes, greetings, opinions, or fun requests are NOT knowledge-based.
                
                STEP 2 — SELECT TOOLS BASED ON CLASSIFICATION.
                
                Rules:
                - If KNOWLEDGE-BASED → include RagSearchTool AND TextAnalysisTool
                - If NOT knowledge-based → include ONLY TextAnalysisTool
                - RagSearchTool must NEVER be used for jokes or casual chat
                
                Respond ONLY in this exact format:
                
                IS_KNOWLEDGE_BASED: <YES or NO>
                
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
