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

        // Simple rule-based tool selection (for now)
        Tool analysisTool = tools.stream()
                .filter(t -> t.name().equals("TextAnalysisTool"))
                .findFirst()
                .orElseThrow();

        String result = analysisTool.execute(goal);
        memory.remember("RESULT: "+result);

        return """
                AGENT PLAN:
                %s
                
                TOOL RESULT:
                %s
                """.formatted(plan, result);
    }

    public List<String> memory() {
        return memory.recall();
    }
}
