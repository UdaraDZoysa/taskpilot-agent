package com.harsha.taskpilot.agent;

import com.harsha.taskpilot.tools.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

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

    public String execute(String goal){
        memory.remember("GOAL: "+goal);

        String plan = taskPlanner.plan(goal);
        memory.remember("PLAN: "+plan);

        // Parse planner output
        boolean needRag = plan.contains("IS_KNOWLEDGE_BASED: YES");

        String ragQuery = "";
        if (needRag) {
            ragQuery = plan.lines()
                    .filter(l -> l.startsWith("QUERY:"))
                    .findFirst()
                    .orElse("QUERY: " + goal)
                    .replace("QUERY:", "")
                    .trim();
        }

        // 3. Find tools
        Tool analysisTool = tools.stream()
                .filter(t -> t.name().equals("TextAnalysisTool"))
                .findFirst()
                .orElseThrow();

        Tool ragTool = tools.stream()
                .filter(t -> t.name().equals("RagSearchTool"))
                .findFirst()
                .orElseThrow();

        // 4. Run RAG only if LLM decided
        String context = "";
        if (needRag) {
            context = ragTool.execute(ragQuery);
            memory.remember("RAG QUERY: " + ragQuery);
            memory.remember("RAG CONTEXT: " + context);
        }

        // 5. Give the LLM everything
        String enrichedGoal = """
            You are an AI agent.
            
            Use the CONTEXT if it helps.
            
            CONTEXT:
            %s
            
            USER GOAL:
            %s
            """.formatted(context, goal);

        // Let the LLM reason using RAG + analysis
        String result = analysisTool.execute(enrichedGoal);
        memory.remember("FINAL RESULT: "+result);

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
                """.formatted(
                                plan,
                                needRag,
                                ragQuery,
                                context,
                                result
                        );
    }

    public List<String> memory() {
        return memory.recall();
    }
}
