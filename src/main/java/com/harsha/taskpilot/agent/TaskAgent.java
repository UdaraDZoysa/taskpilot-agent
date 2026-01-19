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

//    public String execute(String goal) {
//
//        memory.remember("GOAL: " + goal);
//
//        // 1. Ask planner if RAG is needed
//        String plan = taskPlanner.plan(goal);
//        memory.remember("PLAN: " + plan);
//
//        boolean needRag = plan.contains("NEED_RAG: YES");
//
//        String ragContext = "";
//
//        if (needRag) {
//            Tool ragTool = tools.stream()
//                    .filter(t -> t.name().equals("RagSearchTool"))
//                    .findFirst()
//                    .orElseThrow();
//
//            // Extract query from planner output
//            String query = plan.split("QUERY:")[1].split("\n")[0].trim();
//
//            ragContext = ragTool.execute(query);
//            memory.remember("RAG: " + ragContext);
//        }
//
//        // 2. Build final reasoning prompt
//        String finalPrompt = """
//        You are an expert AI.
//
//        Use the CONTEXT below to answer the USER QUESTION.
//        If the context is empty, answer using your own knowledge.
//
//        CONTEXT:
//        %s
//
//        USER QUESTION:
//        %s
//
//        Provide a clear, helpful, well-structured answer.
//        """.formatted(ragContext, goal);
//
//        // 3. Ask the LLM to produce final answer
//        Tool analysisTool = tools.stream()
//                .filter(t -> t.name().equals("TextAnalysisTool"))
//                .findFirst()
//                .orElseThrow();
//
//        String finalAnswer = analysisTool.execute(finalPrompt);
//        memory.remember("FINAL: " + finalAnswer);
//
//        return """
//        AGENT PLAN:
//        %s
//
//        RAG CONTEXT:
//        %s
//
//        FINAL ANSWER:
//        %s
//        """.formatted(plan, ragContext, finalAnswer);
//    }
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
    String finalPrompt = """
                You are a Java expert.

                Use the following KNOWLEDGE BASE if relevant.

                KNOWLEDGE:
                %s

                USER QUESTION:
                %s
                """.formatted(ragContext, goal);

    //Run the reasoning model
    Tool analysisTool = findTool("TextAnalysisTool");
    String answer = analysisTool.execute(finalPrompt);

    memory.remember("FINAL: " + answer);

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
                """.formatted(plan, useRag, ragQuery, ragContext, answer);
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
