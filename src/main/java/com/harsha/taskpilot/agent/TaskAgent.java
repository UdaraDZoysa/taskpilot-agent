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

    boolean useRag = plan.contains("IS_KNOWLEDGE_BASED: YES");
    String ragQuery = extract(plan, "QUERY:");
    String ragContext = "";

    // If RAG is needed → fetch knowledge
    if (useRag && !ragQuery.equals("NONE")) {
        Tool ragTool = findTool("RagSearchTool");
        ragContext = ragTool.execute(ragQuery);
        memory.remember("RAG CONTEXT: " + ragContext);
    }

    //Build final prompt for the reasoning LLM
    String reasoningPrompt = """
                You are a Java expert.

                Use the following KNOWLEDGE BASE if relevant.

                KNOWLEDGE:
                %s

                USER QUESTION:
                %s
                """.formatted(ragContext, goal);

    //Run the reasoning model
    Tool analysisTool = findTool("TextAnalysisTool");
    String draftAnswer = analysisTool.execute(reasoningPrompt);
    memory.remember("DRAFT ANSWER: " + draftAnswer);

    // Decide final output
    String finalAnswer = draftAnswer;
    String reviewResult = "";
    int maxIterations = 3;
    int iteration = 0;

    while (iteration < maxIterations) {
        System.out.println("Review iteration 01: " + iteration);
        System.out.println("Current answer 01: " + finalAnswer);
        String reviewPrompt = """
            You are a senior Java reviewer.
            
            Review the ANSWER below.
            
            If it is correct and complete, respond with:
            STATUS: OK
            
            If it has issues, respond with:
            STATUS: FIX
            IMPROVED_ANSWER: <your improved answer>
            
            ANSWER:
            %s
            """.formatted(finalAnswer);

        reviewResult = analysisTool.execute(reviewPrompt);
        memory.remember("REVIEW " + iteration + ": " + reviewResult);

        if (reviewResult.contains("STATUS: OK")) {
            break;
        }

        if (reviewResult.contains("STATUS: FIX")) {
            finalAnswer = extract(reviewResult, "IMPROVED_ANSWER:");
        }
        System.out.println("Review iteration 02: " + iteration);
        System.out.println("Current answer 02: " + finalAnswer);
        iteration++;
    }

    return """
                AGENT PLAN:
                %s

                RAG USED:
                %s

                RAG QUERY:
                %s

                RAG CONTEXT:
                %s

                FINAL ANSWER:
                %s
                """.formatted(plan, useRag, ragQuery, ragContext, finalAnswer);
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
    public List<String> memory() {
        return memory.recall();
    }
}
