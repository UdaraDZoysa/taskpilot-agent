package com.harsha.taskpilot.agent;

import com.harsha.taskpilot.tools.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskAgent {
    private final TaskPlanner taskPlanner;
    private final List<Tool> tools;
    private final AgentMemory memory;

    public TaskAgent(TaskPlanner taskPlanner,
                     List<Tool> tools,
                     AgentMemory memory) {
        this.taskPlanner = taskPlanner;
        this.tools = tools;
        this.memory = memory;
    }

public String execute(String goal) {

    memory.remember("GOAL: " + goal);

    // Ask planner what to do
    String plan = taskPlanner.plan(goal);
    memory.remember("PLAN: " + plan);

    // Extract tool execution chain
    List<String> toolChain = extractTools(plan);

    String context = "";
    String answer = "";

    // Execute tools in order
    for (String toolName : toolChain) {

        Tool tool = findTool(toolName);

        switch (toolName) {

            case "RagSearchTool" -> {
                context = tool.execute(goal);
                memory.remember("RAG CONTEXT: " + context);
            }

            case "TextAnalysisTool" -> {
                String prompt;

                // Adaptive reasoning prompt
                if (context.isBlank()) {
                    prompt = """
                            You are a friendly assistant.
                            
                            Always produce a clear, complete response.
                            
                            USER REQUEST:
                            %s
                            """.formatted(goal);
                } else {
                    prompt = """
                            You are a Java expert.

                            Use the following KNOWLEDGE BASE to answer accurately.

                            KNOWLEDGE:
                            %s

                            USER QUESTION:
                            %s
                            """.formatted(context, goal);
                }

                answer = tool.execute(prompt);
                memory.remember("DRAFT ANSWER: " + answer);
            }
            default -> throw new IllegalStateException("Unknown tool: " + toolName);
        }
    }

    // ENFORCED self-review (agent policy, not planner)
    boolean isConversational = plan.contains("IS_KNOWLEDGE_BASED: NO");

    // Self-review ONLY for knowledge / reasoning tasks
    if (!isConversational) {

        Tool analysisTool = findTool("TextAnalysisTool");

        String reviewPrompt = """
                You are a senior reviewer.
        
                Review the ANSWER below.
        
                If it is correct and appropriate, respond with:
                STATUS: OK
        
                If it has issues, respond with:
                STATUS: FIX
                IMPROVED_ANSWER: <your improved answer>
        
                ANSWER:
                %s
                """.formatted(answer);

        String review = analysisTool.execute(reviewPrompt);
        memory.remember("REVIEW: " + review);

        if (review.contains("STATUS: FIX")) {
            answer = extract(review, "IMPROVED_ANSWER:");
            memory.remember("FINAL (FIXED): " + answer);
        }
    }

    return """
        AGENT PLAN:
        %s
        
        TOOLS EXECUTED:
        %s
        
        FINAL ANSWER:
        %s
        """.formatted(plan, toolChain, answer);
}

    private Tool findTool(String name) {
        return tools.stream()
                .filter(t -> t.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tool not found: " + name));
    }

    private String extract(String text, String key) {
        for (String line : text.split("\n")) {
            if (line.startsWith(key)) {
                return line.replace(key, "").trim();
            }
        }
        return "NONE";
    }

    private List<String> extractTools(String plan) {
        return plan.lines()
                .filter(line -> line.startsWith("- "))
                .map(line -> line.replace("- ", "").trim())
                .toList();
    }

    public List<String> memory() {
        return memory.recall();
    }
}
