package com.harsha.taskpilot.agent;

import com.harsha.taskpilot.tools.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Component
public class TaskAgent {
    private final TaskPlanner taskPlanner;
    private final List<Tool> tools;

    public TaskAgent(TaskPlanner taskPlanner, List<Tool> tools) {
        this.taskPlanner = taskPlanner;
        this.tools = tools;
    }

    public String execute(String goal){
        String plan = taskPlanner.plan(goal);

        // Simple rule-based tool selection (for now)
        Tool analysisTool = tools.stream()
                .filter(t -> t.name().equals("TextAnalysisTool"))
                .findFirst()
                .orElseThrow();

        String result = analysisTool.execute(goal);

        return """
                AGENT PLAN:
                %s
                
                TOOL RESULT:
                %s
                """.formatted(plan, result);
    }
}
